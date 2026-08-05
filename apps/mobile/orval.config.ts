import { defineConfig } from 'orval'

export default defineConfig({
  apuAsc: {
    input: {
      target: process.env.OPENAPI_URL || 'http://localhost:8081/v3/api-docs',
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
