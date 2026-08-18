#!/usr/bin/env node

import fs from 'node:fs'
import path from 'node:path'

const PUML_DIR = path.resolve('docs/diagrams/puml')
const ADOC_DIR = path.resolve('docs/diagrams/canvases')
const OUTPUT_DIR = path.resolve('docs/diagrams/markdown')

function cleanModuleName(raw) {
  const parts = raw.split('.')
  return parts[parts.length - 1]
}

function normalizePumlFile(filePath) {
  const content = fs.readFileSync(filePath, 'utf8')
  const lines = content.split('\n')

  const headerLines = []
  const componentLines = []
  const relLines = []
  const footerLines = []

  let inContainerBoundary = false

  for (const line of lines) {
    const trimmed = line.trim()
    if (trimmed.startsWith('Component(')) {
      componentLines.push(line)
    } else if (trimmed.startsWith('Rel(')) {
      relLines.push(line)
    } else if (trimmed.startsWith('Container_Boundary(')) {
      inContainerBoundary = true
      headerLines.push(line)
    } else if (trimmed === '}' && inContainerBoundary) {
      inContainerBoundary = false
      // sort components inside boundary
      componentLines.sort((a, b) => a.localeCompare(b))
      headerLines.push(...componentLines)
      headerLines.push(line)
    } else if (trimmed.startsWith('SHOW_LEGEND') || trimmed === '@enduml') {
      footerLines.push(line)
    } else if (!inContainerBoundary && componentLines.length === 0 && relLines.length === 0) {
      headerLines.push(line)
    }
  }

  // Sort relationships deterministically
  relLines.sort((a, b) => a.localeCompare(b))

  const normalized = [
    ...headerLines,
    '',
    ...relLines,
    '',
    ...footerLines,
  ]
    .join('\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim() + '\n'

  fs.writeFileSync(filePath, normalized, 'utf8')
}

function parsePumlToMermaid(pumlContent) {
  const componentRegex = /Component\(([^,]+),\s*"([^"]+)"/g
  const relRegex = /Rel\(([^,]+),\s*([^,]+),\s*"([^"]*)"/g

  const nodes = new Map()
  let match

  while ((match = componentRegex.exec(pumlContent)) !== null) {
    const id = cleanModuleName(match[1].trim())
    const label = match[2].trim()
    nodes.set(id, label)
  }

  const relationships = []
  while ((match = relRegex.exec(pumlContent)) !== null) {
    const source = cleanModuleName(match[1].trim())
    const target = cleanModuleName(match[2].trim())
    const label = match[3].trim()
    relationships.push({ source, target, label })
  }

  if (nodes.size === 0 && relationships.length === 0) {
    return null
  }

  const lines = ['flowchart TD']

  // Declare nodes deterministically sorted
  const sortedNodes = Array.from(nodes.entries()).sort((a, b) => a[0].localeCompare(b[0]))
  for (const [id, label] of sortedNodes) {
    lines.push(`    ${id}["${label} Module"]`)
  }

  // Declare relationships deterministically sorted
  relationships.sort((a, b) => {
    const srcComp = a.source.localeCompare(b.source)
    if (srcComp !== 0) return srcComp
    const tgtComp = a.target.localeCompare(b.target)
    if (tgtComp !== 0) return tgtComp
    return a.label.localeCompare(b.label)
  })

  for (const { source, target, label } of relationships) {
    if (label) {
      lines.push(`    ${source} -->|${label}| ${target}`)
    } else {
      lines.push(`    ${source} --> ${target}`)
    }
  }

  return lines.join('\n')
}

function parseAdocTableToMarkdown(adocContent) {
  if (!adocContent.includes('|===')) {
    return ''
  }

  const tableBlock = adocContent.split('|===')[1]
  if (!tableBlock) return ''

  const lines = tableBlock.split('\n')
  const rows = []
  let currentKey = null
  let currentValueLines = []

  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) continue

    if (trimmed.startsWith('|') && !trimmed.startsWith('|*') && !trimmed.startsWith('|`') && !trimmed.startsWith('|_')) {
      if (currentKey !== null) {
        rows.push({
          key: currentKey,
          value: currentValueLines.join('<br>').trim(),
        })
        currentValueLines = []
      }
      currentKey = trimmed.replace(/^\|\s*/, '')
    } else {
      const content = trimmed.startsWith('|') ? trimmed.replace(/^\|\s*/, '') : trimmed
      currentValueLines.push(content)
    }
  }

  if (currentKey !== null) {
    rows.push({
      key: currentKey,
      value: currentValueLines.join('<br>').trim(),
    })
  }

  if (rows.length === 0) return ''

  const mdTable = [
    '| Specification / Property | Details |',
    '|---|---|',
  ]

  for (const row of rows) {
    const formattedValue = row.value
      .replace(/\* `([^`]+)`/g, '• `$1`')
      .replace(/_([^_]+)_/g, '**$1**')
    mdTable.push(`| **${row.key}** | ${formattedValue || 'None'} |`)
  }

  return mdTable.join('\n')
}

function main() {
  if (!fs.existsSync(PUML_DIR)) {
    console.error(`PUML directory not found: ${PUML_DIR}`)
    process.exit(1)
  }

  fs.mkdirSync(OUTPUT_DIR, { recursive: true })

  const pumlFiles = fs.readdirSync(PUML_DIR).filter((f) => f.endsWith('.puml'))
  const modules = []

  // Step 1: Normalize all PUML files for deterministic git diffs
  for (const file of pumlFiles) {
    normalizePumlFile(path.join(PUML_DIR, file))
  }

  // Step 2: Generate Markdown + Mermaid documents
  for (const file of pumlFiles) {
    const baseName = path.basename(file, '.puml')
    const pumlPath = path.join(PUML_DIR, file)
    const adocPath = path.join(ADOC_DIR, `${baseName}.adoc`)

    const pumlContent = fs.readFileSync(pumlPath, 'utf8')
    const mermaidChart = parsePumlToMermaid(pumlContent)

    let adocTable = ''
    if (fs.existsSync(adocPath)) {
      const adocContent = fs.readFileSync(adocPath, 'utf8')
      adocTable = parseAdocTableToMarkdown(adocContent)
    }

    const title = baseName === 'components' ? 'System Architecture' : `${baseName.replace(/^module-/, '').toUpperCase()} Module`
    const isGlobal = baseName === 'components'

    if (!isGlobal) {
      const modName = baseName.replace(/^module-/, '')
      modules.push({
        name: modName,
        fileName: `${baseName}.md`,
        title,
      })
    }

    const mdContent = [
      `# ${title}`,
      '',
      '## Visual Architecture Diagram',
      '```mermaid',
      mermaidChart || 'flowchart TD\n    Empty["No diagram available"]',
      '```',
      '',
      adocTable ? '## Module Specifications & Boundaries' : '',
      adocTable || '',
      '',
      `> Auto-generated from \`docs/diagrams/puml/${file}\`${fs.existsSync(adocPath) ? ` and \`docs/diagrams/canvases/${baseName}.adoc\`` : ''}.`,
    ].filter(Boolean).join('\n')

    const outFilePath = path.join(OUTPUT_DIR, `${baseName}.md`)
    fs.writeFileSync(outFilePath, mdContent, 'utf8')
  }

  // Sort modules alphabetically
  modules.sort((a, b) => a.name.localeCompare(b.name))

  // Generate master ARCHITECTURE.md linking everything
  const globalPuml = path.join(PUML_DIR, 'components.puml')
  let globalChart = ''
  if (fs.existsSync(globalPuml)) {
    globalChart = parsePumlToMermaid(fs.readFileSync(globalPuml, 'utf8'))
  }

  const masterDoc = [
    '# APU-ASC System Architecture & Modulith Overview',
    '',
    'This documentation is **100% auto-generated** from Spring Modulith architectural analysis.',
    '',
    '## 🌐 Global Architecture Map',
    '```mermaid',
    globalChart,
    '```',
    '',
    '## 📦 Domain Modules Index',
    '| Domain Module | Architecture Diagram & Specification |',
    '|---|---|',
    ...modules.map((m) => `| **${m.name.toUpperCase()}** | [View ${m.name} Specs & Diagram](./${m.fileName}) |`),
    '',
    '---',
    '> Update these documents anytime by running `./manage.sh docs`.',
  ].join('\n')

  fs.writeFileSync(path.join(OUTPUT_DIR, 'README.md'), masterDoc, 'utf8')
  fs.writeFileSync(path.join(OUTPUT_DIR, 'ARCHITECTURE.md'), masterDoc, 'utf8')

  console.log(`✓ [OK] Successfully normalized PUML and generated Markdown + Mermaid in docs/diagrams/markdown/`)
}

main()
