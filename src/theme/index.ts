import { Dimensions } from 'react-native';

const { width, height } = Dimensions.get('window');

export const COLORS = {
  // Primarios
  primary: '#7B6EFA',
  primaryDark: '#5A4FD0',
  primaryLight: '#A89BFC',

  // Secundarios
  secondary: '#FA6E9F',
  secondaryLight: '#FC9ABE',

  // Fondo
  background: '#080810',
  backgroundCard: '#111120',
  backgroundInput: '#0C0C1A',
  surface: '#18182A',

  // Texto
  textPrimary: '#F0F0FF',
  textSecondary: '#8888AA',
  textMuted: '#44445A',
  textDark: '#080810',

  // Estado
  success: '#3EC97E',
  warning: '#FFA94D',
  error: '#FF5C6A',
  info: '#5BC4F7',

  // Bordes
  border: '#1E1E32',
  borderLight: '#2A2A44',

  // Gradientes base
  gradientStart: '#7B6EFA',
  gradientEnd: '#FA6E9F',

  white: '#FFFFFF',
  black: '#000000',
  transparent: 'transparent',
};

export const GRADIENTS = {
  primary: ['#7B6EFA', '#5A4FD0'],
  secondary: ['#FA6E9F', '#CC4477'],
  success: ['#3EC97E', '#2A8F58'],
  card: ['#111120', '#0C0C1A'],
  header: ['#1A1A2E', '#0D0D1A'],
  headerAccent: ['#7B6EFA', '#5A4FD0'],
  ticket: ['#18182A', '#111120'],
  factura: ['#111120', '#080810'],
  action1: ['#7B6EFA', '#5A4FD0'],
  action2: ['#FA6E9F', '#CC4477'],
  action3: ['#3EC97E', '#2A8F58'],
  action4: ['#FFA94D', '#CC7722'],
};

export const FONTS = {
  thin: 'Roboto-Thin',
  light: 'Roboto-Light',
  regular: 'Roboto-Regular',
  medium: 'Roboto-Medium',
  bold: 'Roboto-Bold',
  black: 'Roboto-Black',
};

export const SIZES = {
  // Fuentes
  xs: 10,
  sm: 12,
  md: 14,
  base: 16,
  lg: 18,
  xl: 20,
  xxl: 24,
  xxxl: 30,
  h1: 36,
  h2: 30,
  h3: 24,
  h4: 20,

  // Espaciado
  padding: 16,
  paddingLg: 24,
  paddingSm: 8,
  margin: 16,
  marginLg: 24,
  marginSm: 8,

  // Bordes
  radius: 14,
  radiusLg: 20,
  radiusSm: 10,
  radiusFull: 999,

  // Pantalla
  width,
  height,
};

export const SHADOWS = {
  sm: {
    shadowColor: '#7B6EFA',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 6,
    elevation: 4,
  },
  md: {
    shadowColor: '#7B6EFA',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 12,
    elevation: 8,
  },
  lg: {
    shadowColor: '#7B6EFA',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.3,
    shadowRadius: 20,
    elevation: 14,
  },
  dark: {
    shadowColor: '#000000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.5,
    shadowRadius: 12,
    elevation: 8,
  },
};

const theme = { COLORS, FONTS, SIZES, SHADOWS, GRADIENTS };
export default theme;
