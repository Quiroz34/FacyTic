import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createStackNavigator } from '@react-navigation/stack';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';

import { COLORS, SIZES, SHADOWS } from '../theme';
import { HomeScreen }      from '../screens/Home/HomeScreen';
import { TicketsScreen }   from '../screens/Tickets/TicketsScreen';
import { FacturasScreen }  from '../screens/Facturas/FacturasScreen';
import { ProductosScreen } from '../screens/Productos/ProductosScreen';
import { EmpresaScreen }   from '../screens/Empresa/EmpresaScreen';

import { NuevoProductoScreen } from '../screens/Productos/NuevoProductoScreen';
import { NuevaFacturaScreen }  from '../screens/Facturas/NuevaFacturaScreen';
import { NuevoTicketScreen }   from '../screens/Tickets/NuevoTicketScreen';
import { DetalleFacturaScreen } from '../screens/Facturas/DetalleFacturaScreen';
import { DetalleTicketScreen } from '../screens/Tickets/DetalleTicketScreen';

const Tab   = createBottomTabNavigator();
const Stack = createStackNavigator();

const TAB_ITEMS = [
  { name: 'Inicio',    icon: 'home-variant',                   screen: HomeScreen },
  { name: 'Tickets',   icon: 'ticket-confirmation-outline',    screen: TicketsScreen },
  { name: 'Facturas',  icon: 'file-document-outline',          screen: FacturasScreen },
  { name: 'Productos', icon: 'package-variant-closed-outline', screen: ProductosScreen },
  { name: 'Empresa',   icon: 'domain',                        screen: EmpresaScreen },
];

const CustomTabBar = ({ state, navigation }: any) => (
  <View style={styles.tabBar}>
    {state.routes.map((route: any, index: number) => {
      const isFocused = state.index === index;
      const item = TAB_ITEMS[index];
      const color = isFocused ? COLORS.primary : COLORS.textMuted;

      return (
        <TouchableOpacity
          key={route.key}
          onPress={() => navigation.navigate(route.name)}
          style={styles.tabItem}
          activeOpacity={0.7}
        >
          {isFocused && <View style={styles.tabActiveBackground} />}
          <Icon name={item.icon} size={22} color={color} />
          <Text style={[styles.tabLabel, isFocused && styles.tabLabelActive]}>
            {item.name}
          </Text>
        </TouchableOpacity>
      );
    })}
  </View>
);

const headerStyle = {
  backgroundColor: COLORS.background,
  borderBottomWidth: 1,
  borderBottomColor: COLORS.border,
  elevation: 0,
  shadowOpacity: 0,
};

const screenOptions = {
  headerStyle,
  headerTintColor: COLORS.textPrimary,
  headerTitleStyle: { fontWeight: '700' as const, fontSize: SIZES.base, color: COLORS.textPrimary },
  headerBackTitleVisible: false,
};

const TabNavigator = () => (
  <Tab.Navigator tabBar={props => <CustomTabBar {...props} />} screenOptions={screenOptions}>
    {TAB_ITEMS.map(item => (
      <Tab.Screen key={item.name} name={item.name} component={item.screen} />
    ))}
  </Tab.Navigator>
);

export const AppNavigator = () => (
  <NavigationContainer>
    <Stack.Navigator screenOptions={screenOptions}>
      <Stack.Screen name="Main" component={TabNavigator} options={{ headerShown: false }} />
      <Stack.Screen name="NuevoProducto" component={NuevoProductoScreen} options={{ title: 'Producto' }} />
      <Stack.Screen name="NuevaFactura" component={NuevaFacturaScreen} options={{ title: 'Nueva Factura' }} />
      <Stack.Screen name="NuevoTicket" component={NuevoTicketScreen} options={{ title: 'Nuevo Ticket' }} />
      <Stack.Screen name="DetalleFactura" component={DetalleFacturaScreen} options={{ title: 'Factura' }} />
      <Stack.Screen name="DetalleTicket" component={DetalleTicketScreen} options={{ title: 'Ticket' }} />
    </Stack.Navigator>
  </NavigationContainer>
);

const styles = StyleSheet.create({
  tabBar: {
    flexDirection: 'row',
    backgroundColor: COLORS.backgroundCard,
    paddingBottom: 10,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: COLORS.border,
    ...SHADOWS.dark,
  },
  tabItem: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 6,
    position: 'relative',
  },
  tabActiveBackground: {
    position: 'absolute',
    top: 0,
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: COLORS.primary + '18',
  },
  tabLabel: {
    color: COLORS.textMuted,
    fontSize: SIZES.xs,
    fontWeight: '500',
    marginTop: 3,
  },
  tabLabelActive: {
    color: COLORS.primary,
    fontWeight: '700',
  },
});
