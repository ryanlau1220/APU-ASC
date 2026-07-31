import { defineConfig } from 'orval'

export default defineConfig({
  apuAsc: {
    input: {
      target: './openapi.json',
    },
    output: {
      mode: 'split',
      target: './src/api/generated/endpoints.ts',
      schemas: './src/api/generated/models',
      client: 'react-query',
      override: {
        mutator: {
          path: './src/lib/apiClient.ts',
          name: 'customInstance',
        },
      },
    },
  },
})
