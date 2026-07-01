import React from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';
import { useAppStore } from '../../store/useAppStore';
import { Factura } from '../../types';

const estadoConfig = {
  pendiente: { color: COLORS.warning, icon: 'clock-outline' },
  pagada:    { color: COLORS.success, icon: 'check-circle-outline' },
  cancelada: { color: COLORS.error,   icon: 'close-circle-outline' },
};

const FacturaCard = ({ factura, onPress }: { factura: Factura; onPress: () => void }) => {
  const cfg = estadoConfig[factura.estado] ?? estadoConfig.pendiente;
  return (
    <TouchableOpacity onPress={onPress} style={[styles.card, SHADOWS.sm]} activeOpacity={0.75}>
      <View style={[styles.cardAccent, { backgroundColor: cfg.color }]} />
      <View style={styles.cardBody}>
        <View style={[styles.cardIconBox, { backgroundColor: cfg.color + '18' }]}>
          <Icon name="file-document-outline" size={22} color={cfg.color} />
        </View>
        <View style={styles.cardCenter}>
          <View style={styles.headerRow}>
            <Text style={styles.numero}>{factura.numero}</Text>
            <View style={[styles.badge, { backgroundColor: cfg.color + '22', borderColor: cfg.color + '55' }]}>
              <Icon name={cfg.icon} size={10} color={cfg.color} />
              <Text style={[styles.badgeText, { color: cfg.color }]}> {factura.estado}</Text>
            </View>
          </View>
          <Text style={styles.cliente}>{factura.clienteNombre ?? 'Consumidor Final'}</Text>
          <Text style={styles.fecha}>{factura.fecha}</Text>
        </View>
        <View style={styles.cardRight}>
          <Text style={styles.total}>${factura.total.toFixed(2)}</Text>
          <Icon name="chevron-right" size={18} color={COLORS.textMuted} style={{ marginTop: 4 }} />
        </View>
      </View>
    </TouchableOpacity>
  );
};

const ListHeader = ({ count }: { count: number }) => (
  <View style={styles.listHeader}>
    <Text style={styles.listHeaderTitle}>Facturas</Text>
    <View style={styles.countBadge}>
      <Text style={styles.countBadgeText}>{count}</Text>
    </View>
  </View>
);

export const FacturasScreen = ({ navigation }: any) => {
  const { facturas } = useAppStore();

  return (
    <View style={styles.container}>
      <FlatList
        data={facturas}
        keyExtractor={f => String(f.id)}
        renderItem={({ item }) => (
          <FacturaCard
            factura={item}
            onPress={() => navigation.navigate('DetalleFactura', { id: item.id })}
          />
        )}
        ListHeaderComponent={<ListHeader count={facturas.length} />}
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.empty}>
            <View style={styles.emptyIconBox}>
              <Icon name="file-document-outline" size={48} color={COLORS.textMuted} />
            </View>
            <Text style={styles.emptyText}>Sin facturas aún</Text>
            <Text style={styles.emptySub}>Crea tu primera factura</Text>
          </View>
        }
      />

      <TouchableOpacity
        style={styles.fab}
        onPress={() => navigation.navigate('NuevaFactura')}
        activeOpacity={0.8}
      >
        <LinearGradient colors={GRADIENTS.primary} style={styles.fabGradient}>
          <Icon name="plus" size={26} color={COLORS.white} />
        </LinearGradient>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  list: { paddingHorizontal: SIZES.padding, paddingBottom: 100 },
  listHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    paddingTop: SIZES.padding,
    paddingBottom: SIZES.paddingSm,
  },
  listHeaderTitle: {
    color: COLORS.textPrimary,
    fontSize: SIZES.xl,
    fontWeight: '800',
    letterSpacing: -0.3,
  },
  countBadge: {
    backgroundColor: COLORS.primary + '22',
    borderRadius: SIZES.radiusFull,
    paddingHorizontal: 9,
    paddingVertical: 2,
    borderWidth: 1,
    borderColor: COLORS.primary + '44',
  },
  countBadgeText: { color: COLORS.primary, fontSize: SIZES.xs, fontWeight: '700' },
  card: {
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radius,
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
    borderWidth: 1,
    borderColor: COLORS.border,
    overflow: 'hidden',
  },
  cardAccent: { width: 4, alignSelf: 'stretch' },
  cardBody: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    padding: SIZES.padding,
  },
  cardIconBox: {
    width: 44,
    height: 44,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  cardCenter: { flex: 1 },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 3,
  },
  numero: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '700' },
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: SIZES.radiusFull,
    borderWidth: 1,
  },
  badgeText: { fontSize: SIZES.xs, fontWeight: '700', textTransform: 'capitalize' },
  cliente: { color: COLORS.textSecondary, fontSize: SIZES.sm },
  fecha: { color: COLORS.textMuted, fontSize: SIZES.xs, marginTop: 2 },
  cardRight: { alignItems: 'flex-end', marginLeft: 8 },
  total: { color: COLORS.primary, fontSize: SIZES.lg, fontWeight: '800' },
  empty: { alignItems: 'center', justifyContent: 'center', paddingTop: 80 },
  emptyIconBox: {
    width: 96,
    height: 96,
    borderRadius: 48,
    backgroundColor: COLORS.backgroundCard,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  emptyText: { color: COLORS.textPrimary, fontSize: SIZES.xl, fontWeight: '700' },
  emptySub: { color: COLORS.textSecondary, fontSize: SIZES.md, marginTop: 6 },
  fab: {
    position: 'absolute',
    bottom: 24,
    right: 24,
    borderRadius: 32,
    ...SHADOWS.lg,
  },
  fabGradient: {
    width: 60,
    height: 60,
    borderRadius: 30,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
