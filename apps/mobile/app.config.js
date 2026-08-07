const usesLocalBackend = process.env.EXPO_PUBLIC_API_BASE_URL?.startsWith('http://') ?? false

module.exports = {
  name: 'APU-ASC',
  slug: 'apu-asc-mobile',
  version: '1.0.0',
  orientation: 'portrait',
  icon: './assets/icon.png',
  scheme: 'com.apuasc.app',
  userInterfaceStyle: 'automatic',
  splash: {
    image: './assets/icon.png',
    resizeMode: 'contain',
    backgroundColor: '#ffffff',
  },
  ios: {
    bundleIdentifier: 'com.apuasc.mobile',
    supportsTablet: true,
  },
  android: {
    package: 'com.apuasc.mobile',
    usesCleartextTraffic: usesLocalBackend,
    adaptiveIcon: {
      backgroundColor: '#2563EB',
      foregroundImage: './assets/icon-foreground.png',
    },
  },
  plugins: ['expo-router', 'expo-secure-store'],
  experiments: {
    typedRoutes: true,
    reactCompiler: true,
  },
  runtimeVersion: {
    policy: 'appVersion',
  },
  extra: {
    eas: {
      projectId: '6f82268a-9a40-492c-8434-0abd614f14ab',
    },
  },
}
