import React from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';
import { useAppStore } from '../../store/useAppStore';
import { Ticket } from '../../types';

const TicketCard = ({ ticket, onPress }: { ticket: Ticket; onPress: () => void }) => (
  <TouchableOpacity onPress={onPress} style={[styles.card, SHADOWS.sm]} activeOpacity={0.75}>
    <View style={styles.cardAccent} />
    <View style={styles.cardBody}>
      <View style={styles.cardIconBox}>
        <Icon name="ticket-confirmation-outline" size={22} color={COLORS.secondary} />
      </View>
      <View style={styles.cardCenter}>
        <Text style={styles.numero}>{ticket.numero}</Text>
        <Text style={styles.fecha}>{ticket.fecha}</Text>
        <View style={styles.itemChip}>
          <Icon name="package-variant-closed-outline" size={11} color={COLORS.textMuted} />
          <Text style={styles.itemCount}> {ticket.items.length} producto(s)</Text>
        </View>
      </View>
      <View style={styles.cardRight}>
        <Text style={styles.total}>${ticket.total.toFixed(2)}</Text>
        <Icon name="chevron-right" size={18} color={COLORS.textMuted} style={{ marginTop: 4 }} />
      </View>
    </View>
  </TouchableOpacity>
);

const ListHeader = ({ count }: { count: number }) => (
  <View style={styles.listHeader}>
    <Text style={styles.listHeaderTitle}>Tickets</Text>
    <View style={styles.countBadge}>
      <Text style={styles.countBadgeText}>{count}</Text>
    </View>
  </View>
);

export const TicketsScreen = ({ navigation }: any) => {
  const { tickets } = useAppStore();

  return (
    <View style={styles.container}>
      <FlatList
        data={tickets}
        keyExtractor={t => String(t.id)}
        renderItem={({ item }) => (
          <TicketCard
            ticket={item}
            onPress={() => navigation.navigate('DetalleTicket', { id: item.id })}
          />
        )}
        ListHeaderComponent={<ListHeader count={tickets.length} />}
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.empty}>
            <View style={styles.emptyIconBox}>
              <Icon name="ticket-confirmation-outline" size={48} color={COLORS.textMuted} />
            </View>
            <Text style={styles.emptyText}>Sin tickets aún</Text>
            <Text style={styles.emptySub}>Crea tu primer ticket de venta</Text>
          </View>
        }
      />

      <TouchableOpacity
        style={styles.fab}
        onPress={() => navigation.navigate('NuevoTicket')}
        activeOpacity={0.8}
      >
        <LinearGradient colors={GRADIENTS.secondary} style={styles.fabGradient}>
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
    backgroundColor: COLORS.secondary + '22',
    borderRadius: SIZES.radiusFull,
    paddingHorizontal: 9,
    paddingVertical: 2,
    borderWidth: 1,
    borderColor: COLORS.secondary + '44',
  },
  countBadgeText: { color: COLORS.secondary, fontSize: SIZES.xs, fontWeight: '700' },
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
  cardAccent: {
    width: 4,
    alignSelf: 'stretch',
    backgroundColor: COLORS.secondary,
  },
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
    backgroundColor: COLORS.secondary + '18',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  cardCenter: { flex: 1 },
  numero: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '700' },
  fecha: { color: COLORS.textSecondary, fontSize: SIZES.sm, marginTop: 2 },
  itemChip: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 4,
  },
  itemCount: { color: COLORS.textMuted, fontSize: SIZES.xs },
  cardRight: { alignItems: 'flex-end' },
  total: { color: COLORS.secondary, fontSize: SIZES.lg, fontWeight: '800' },
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
