import type { ExpoConfig } from 'expo/config'

const config: ExpoConfig = {
  name: 'APU-ASC',
  slug: 'apu-asc-mobile',
  version: '1.0.0',
  orientation: 'portrait',
  icon: './assets/icon.png',
  scheme: 'com.apuasc.app',
  userInterfaceStyle: 'automatic',
  ios: {
    bundleIdentifier: 'com.apuasc.mobile',
    supportsTablet: true,
  },
    android: {
      package: 'com.apuasc.mobile',
      usesCleartextTraffic: process.env.EXPO_PUBLIC_API_BASE_URL?.startsWith('http://') ?? false,
      adaptiveIcon: {
      backgroundColor: '#2563EB',
        foregroundImage: './assets/icon.png',
      },
    } as NonNullable<ExpoConfig['android']> & { usesCleartextTraffic: boolean },
  plugins: ['expo-router', 'expo-secure-store'],
  experiments: {
    typedRoutes: true,
    reactCompiler: true,
  },
  runtimeVersion: {
    policy: 'appVersion',
  },
}

export default config
