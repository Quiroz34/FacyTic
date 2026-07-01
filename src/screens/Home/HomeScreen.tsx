import React from 'react';
import {
  View, Text, ScrollView, TouchableOpacity,
  StyleSheet, StatusBar, Image,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';
import { useAppStore } from '../../store/useAppStore';

const StatCard = ({
  label, value, color, iconName, formatCurrency,
}: {
  label: string;
  value: number;
  color: string;
  iconName: string;
  formatCurrency?: boolean;
}) => (
  <View style={[styles.statCard, SHADOWS.sm]}>
    <View style={[styles.statIconBox, { backgroundColor: color + '22' }]}>
      <Icon name={iconName} size={20} color={color} />
    </View>
    <Text style={[styles.statValue, { color }]}>
      {formatCurrency ? `$${value.toFixed(2)}` : value}
    </Text>
    <Text style={styles.statLabel}>{label}</Text>
  </View>
);

const QUICK_ACTIONS = [
  { icon: 'file-document-outline', label: 'Nueva\nFactura', gradient: GRADIENTS.action1, nav: 'NuevaFactura' },
  { icon: 'ticket-confirmation-outline', label: 'Nuevo\nTicket', gradient: GRADIENTS.action2, nav: 'NuevoTicket' },
  { icon: 'package-variant-closed-outline', label: 'Productos', gradient: GRADIENTS.action3, nav: 'Productos' },
  { icon: 'domain', label: 'Mi Empresa', gradient: GRADIENTS.action4, nav: 'Empresa' },
];

const estadoColor = (estado: string) => {
  if (estado === 'pagada') return COLORS.success;
  if (estado === 'cancelada') return COLORS.error;
  return COLORS.warning;
};

export const HomeScreen = ({ navigation }: any) => {
  const { empresa, facturas, tickets } = useAppStore();

  const totalFacturas = facturas.length;
  const totalTickets = tickets.length;
  const totalCobrado = facturas
    .filter(f => f.estado === 'pagada')
    .reduce((acc, f) => acc + f.total, 0);

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={COLORS.background} />

      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>Bienvenido de vuelta</Text>
          <Text style={styles.empresaNombre}>{empresa?.nombre ?? 'Mi Negocio'}</Text>
          {empresa?.slogan ? (
            <Text style={styles.slogan}>{empresa.slogan}</Text>
          ) : null}
        </View>
        {empresa?.logo ? (
          <Image source={{ uri: empresa.logo }} style={styles.avatarLogo} resizeMode="cover" />
        ) : (
          <LinearGradient colors={GRADIENTS.primary} style={styles.avatarCircle}>
            <Text style={styles.avatarText}>
              {empresa?.nombre ? empresa.nombre[0].toUpperCase() : 'N'}
            </Text>
          </LinearGradient>
        )}
      </View>

      <ScrollView
        style={styles.content}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* Stats */}
        <Text style={styles.sectionTitle}>Resumen</Text>
        <View style={styles.statsRow}>
          <StatCard
            label="Facturas"
            value={totalFacturas}
            color={COLORS.primary}
            iconName="file-document-outline"
          />
          <StatCard
            label="Tickets"
            value={totalTickets}
            color={COLORS.secondary}
            iconName="ticket-confirmation-outline"
          />
          <StatCard
            label="Cobrado"
            value={totalCobrado}
            color={COLORS.success}
            iconName="cash-multiple"
            formatCurrency
          />
        </View>

        {/* Acciones rápidas */}
        <Text style={styles.sectionTitle}>Acciones rápidas</Text>
        <View style={styles.quickActionsGrid}>
          {QUICK_ACTIONS.map(action => (
            <TouchableOpacity
              key={action.nav}
              onPress={() => navigation.navigate(action.nav)}
              style={styles.quickActionWrapper}
              activeOpacity={0.75}
            >
              <LinearGradient
                colors={action.gradient}
                start={{ x: 0, y: 0 }}
                end={{ x: 1, y: 1 }}
                style={[styles.quickAction, SHADOWS.md]}
              >
                <View style={styles.quickActionIconBox}>
                  <Icon name={action.icon} size={26} color={COLORS.white} />
                </View>
                <Text style={styles.quickActionLabel}>{action.label}</Text>
              </LinearGradient>
            </TouchableOpacity>
          ))}
        </View>

        {/* Últimas facturas */}
        {facturas.length > 0 && (
          <>
            <View style={styles.sectionRow}>
              <Text style={styles.sectionTitle}>Últimas facturas</Text>
              <TouchableOpacity onPress={() => navigation.navigate('Facturas')}>
                <Text style={styles.seeAll}>Ver todas</Text>
              </TouchableOpacity>
            </View>
            {facturas.slice(0, 3).map(f => {
              const color = estadoColor(f.estado);
              return (
                <TouchableOpacity
                  key={f.id}
                  style={[styles.listItem, SHADOWS.sm]}
                  onPress={() => navigation.navigate('DetalleFactura', { id: f.id })}
                  activeOpacity={0.75}
                >
                  <View style={[styles.listItemAccent, { backgroundColor: color }]} />
                  <View style={styles.listItemBody}>
                    <View>
                      <Text style={styles.listItemTitle}>{f.numero}</Text>
                      <Text style={styles.listItemSub}>{f.clienteNombre ?? 'Consumidor Final'}</Text>
                    </View>
                    <View style={styles.listItemRight}>
                      <Text style={styles.listItemAmount}>${f.total.toFixed(2)}</Text>
                      <View style={[styles.badge, { backgroundColor: color + '28', borderColor: color + '55' }]}>
                        <Text style={[styles.badgeText, { color }]}>{f.estado}</Text>
                      </View>
                    </View>
                  </View>
                </TouchableOpacity>
              );
            })}
          </>
        )}
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: 52,
    paddingBottom: 20,
    paddingHorizontal: SIZES.paddingLg,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
  },
  greeting: { color: COLORS.textMuted, fontSize: SIZES.sm, fontWeight: '500' },
  empresaNombre: {
    color: COLORS.textPrimary,
    fontSize: SIZES.xxl,
    fontWeight: '800',
    marginTop: 2,
    letterSpacing: -0.5,
  },
  slogan: { color: COLORS.textSecondary, fontSize: SIZES.sm, marginTop: 2 },
  avatarCircle: {
    width: 46,
    height: 46,
    borderRadius: 23,
    alignItems: 'center',
    justifyContent: 'center',
    ...SHADOWS.md,
  },
  avatarLogo: {
    width: 46,
    height: 46,
    borderRadius: 23,
    borderWidth: 2,
    borderColor: COLORS.primary,
    ...SHADOWS.md,
  },
  avatarText: { color: COLORS.white, fontSize: SIZES.xl, fontWeight: '800' },
  content: { flex: 1 },
  contentContainer: { padding: SIZES.padding, paddingBottom: 100 },
  sectionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: SIZES.marginLg,
    marginBottom: SIZES.marginSm,
  },
  sectionTitle: {
    color: COLORS.textSecondary,
    fontSize: SIZES.xs,
    fontWeight: '700',
    letterSpacing: 1.2,
    textTransform: 'uppercase',
    marginTop: SIZES.marginLg,
    marginBottom: SIZES.marginSm,
  },
  seeAll: { color: COLORS.primary, fontSize: SIZES.sm, fontWeight: '600' },
  statsRow: { flexDirection: 'row', gap: 10 },
  statCard: {
    flex: 1,
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radius,
    padding: 14,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  statIconBox: {
    width: 38,
    height: 38,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  statValue: { fontSize: SIZES.xl, fontWeight: '800' },
  statLabel: { color: COLORS.textSecondary, fontSize: SIZES.xs, marginTop: 3, fontWeight: '500' },
  quickActionsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12 },
  quickActionWrapper: { width: '47%' },
  quickAction: {
    borderRadius: SIZES.radiusLg,
    padding: SIZES.paddingLg,
    alignItems: 'flex-start',
    justifyContent: 'flex-end',
    minHeight: 110,
  },
  quickActionIconBox: {
    width: 44,
    height: 44,
    borderRadius: 12,
    backgroundColor: 'rgba(255,255,255,0.18)',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 12,
  },
  quickActionLabel: {
    color: COLORS.white,
    fontSize: SIZES.sm,
    fontWeight: '700',
    lineHeight: 18,
  },
  listItem: {
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radius,
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
    borderWidth: 1,
    borderColor: COLORS.border,
    overflow: 'hidden',
  },
  listItemAccent: { width: 4, alignSelf: 'stretch' },
  listItemBody: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: SIZES.padding,
  },
  listItemTitle: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '700' },
  listItemSub: { color: COLORS.textSecondary, fontSize: SIZES.sm, marginTop: 2 },
  listItemRight: { alignItems: 'flex-end', gap: 6 },
  listItemAmount: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '800' },
  badge: {
    paddingHorizontal: 9,
    paddingVertical: 3,
    borderRadius: SIZES.radiusFull,
    borderWidth: 1,
  },
  badgeText: { fontSize: SIZES.xs, fontWeight: '700', textTransform: 'capitalize' },
});
