import React, { useEffect, useState } from 'react';
import { View, Text, ActivityIndicator, TouchableOpacity, StyleSheet, Image, StatusBar } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { initDatabase } from './src/database';
import { useAppStore } from './src/store/useAppStore';
import { AppNavigator } from './src/navigation';
import { COLORS, SIZES } from './src/theme';

const SplashScreen = () => (
  <View style={splash.container}>
    <StatusBar barStyle="light-content" backgroundColor="transparent" translucent />
    <Image 
      source={require('./src/assets/images/splash.png')} 
      style={StyleSheet.absoluteFillObject} 
      resizeMode="contain"
    />
    <View style={splash.loaderContainer}>
      <ActivityIndicator color={COLORS.primary} size="large" />
    </View>
  </View>
);

const ErrorScreen = ({ onRetry }: { onRetry: () => void }) => (
  <View style={splash.container}>
    <Icon name="alert-circle-outline" size={64} color={COLORS.error} style={{ marginBottom: 16 }} />
    <Text style={splash.errorTitle}>Error al iniciar</Text>
    <Text style={splash.errorSub}>No se pudo inicializar la base de datos.{'\n'}Verifica que la app tenga permisos de almacenamiento.</Text>
    <TouchableOpacity onPress={onRetry} style={splash.retryBtn}>
      <Text style={splash.retryText}>Reintentar</Text>
    </TouchableOpacity>
  </View>
);

function App() {
  const [ready, setReady] = useState(false);
  const [dbError, setDbError] = useState(false);
  const { initAll } = useAppStore();

  const bootstrap = async () => {
    setDbError(false);
    setReady(false);
    try {
      await initDatabase();
      initAll();
    } catch (error) {
      console.error('[App] Error al iniciar la base de datos:', error);
      setDbError(true);
    } finally {
      setReady(true);
    }
  };

  useEffect(() => {
    bootstrap();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!ready) return <SplashScreen />;
  if (dbError) return <ErrorScreen onRetry={bootstrap} />;

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <AppNavigator />
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

const splash = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#080810', // Coincide con el fondo oscuro de la marca
  },
  loaderContainer: {
    position: 'absolute',
    bottom: 50,
    width: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    color: COLORS.textPrimary,
    fontSize: SIZES.h2,
    fontWeight: '800',
    letterSpacing: -0.5,
  },
  errorTitle: {
    color: COLORS.textPrimary,
    fontSize: SIZES.xl,
    fontWeight: '700',
    marginBottom: 10,
  },
  errorSub: {
    color: COLORS.textSecondary,
    fontSize: SIZES.base,
    textAlign: 'center',
    lineHeight: 22,
  },
  retryBtn: {
    marginTop: 28,
    backgroundColor: COLORS.primary,
    paddingHorizontal: 32,
    paddingVertical: 14,
    borderRadius: SIZES.radiusLg,
  },
  retryText: {
    color: COLORS.white,
    fontSize: SIZES.base,
    fontWeight: '700',
  },
});

export default App;
