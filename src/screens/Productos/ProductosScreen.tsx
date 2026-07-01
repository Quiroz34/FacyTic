import React, { useState } from 'react';
import {
  View, Text, FlatList, TouchableOpacity,
  StyleSheet, TextInput,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';
import { useAppStore } from '../../store/useAppStore';
import { Producto } from '../../types';

const ICON_COLORS = [
  COLORS.primary, COLORS.secondary, COLORS.success, COLORS.warning, COLORS.info,
];

const ProductoCard = ({
  producto, index, onPress,
}: { producto: Producto; index: number; onPress: () => void }) => {
  const accentColor = ICON_COLORS[index % ICON_COLORS.length];
  return (
    <TouchableOpacity onPress={onPress} style={[styles.card, SHADOWS.sm]} activeOpacity={0.75}>
      <View style={[styles.iconBox, { backgroundColor: accentColor + '20' }]}>
        <Icon name="package-variant-closed" size={22} color={accentColor} />
      </View>
      <View style={styles.info}>
        <Text style={styles.nombre}>{producto.nombre}</Text>
        {producto.descripcion ? (
          <Text style={styles.descripcion} numberOfLines={1}>{producto.descripcion}</Text>
        ) : null}
      </View>
      <View style={styles.priceCol}>
        <Text style={styles.precio}>${producto.precio.toFixed(2)}</Text>
        <Icon name="chevron-right" size={16} color={COLORS.textMuted} />
      </View>
    </TouchableOpacity>
  );
};

export const ProductosScreen = ({ navigation }: any) => {
  const { productos } = useAppStore();
  const [search, setSearch] = useState('');
  const [focused, setFocused] = useState(false);

  const filtrados = productos.filter(p =>
    p.nombre.toLowerCase().includes(search.toLowerCase()),
  );

  return (
    <View style={styles.container}>
      {/* Search */}
      <View style={[styles.searchBox, focused && styles.searchBoxFocused]}>
        <Icon name="magnify" size={20} color={focused ? COLORS.primary : COLORS.textMuted} />
        <TextInput
          style={styles.searchInput}
          placeholder="Buscar producto o servicio..."
          placeholderTextColor={COLORS.textMuted}
          value={search}
          onChangeText={setSearch}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
        />
        {search.length > 0 && (
          <TouchableOpacity onPress={() => setSearch('')}>
            <Icon name="close-circle" size={18} color={COLORS.textMuted} />
          </TouchableOpacity>
        )}
      </View>

      <FlatList
        data={filtrados}
        keyExtractor={p => String(p.id)}
        renderItem={({ item, index }) => (
          <ProductoCard
            producto={item}
            index={index}
            onPress={() => navigation.navigate('NuevoProducto', { id: item.id })}
          />
        )}
        ListHeaderComponent={
          <View style={styles.listHeader}>
            <Text style={styles.listHeaderTitle}>Catálogo</Text>
            <View style={styles.countBadge}>
              <Text style={styles.countBadgeText}>{filtrados.length}</Text>
            </View>
          </View>
        }
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.empty}>
            <View style={styles.emptyIconBox}>
              <Icon name="package-variant-closed-outline" size={48} color={COLORS.textMuted} />
            </View>
            <Text style={styles.emptyText}>
              {search ? 'Sin resultados' : 'Sin productos aún'}
            </Text>
            <Text style={styles.emptySub}>
              {search ? `No hay productos con "${search}"` : 'Agrega tu primer producto o servicio'}
            </Text>
          </View>
        }
      />

      <TouchableOpacity
        style={styles.fab}
        onPress={() => navigation.navigate('NuevoProducto')}
        activeOpacity={0.8}
      >
        <LinearGradient colors={GRADIENTS.success} style={styles.fabGradient}>
          <Icon name="plus" size={26} color={COLORS.white} />
        </LinearGradient>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  searchBox: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.backgroundInput,
    margin: SIZES.margin,
    marginBottom: 4,
    borderRadius: SIZES.radiusSm,
    paddingHorizontal: SIZES.padding,
    borderWidth: 1.5,
    borderColor: COLORS.border,
    gap: 10,
  },
  searchBoxFocused: { borderColor: COLORS.primary },
  searchInput: {
    flex: 1,
    color: COLORS.textPrimary,
    fontSize: SIZES.base,
    paddingVertical: 13,
  },
  list: { paddingHorizontal: SIZES.padding, paddingBottom: 100 },
  listHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    paddingTop: SIZES.paddingSm,
    paddingBottom: SIZES.paddingSm,
  },
  listHeaderTitle: {
    color: COLORS.textPrimary,
    fontSize: SIZES.xl,
    fontWeight: '800',
    letterSpacing: -0.3,
  },
  countBadge: {
    backgroundColor: COLORS.success + '22',
    borderRadius: SIZES.radiusFull,
    paddingHorizontal: 9,
    paddingVertical: 2,
    borderWidth: 1,
    borderColor: COLORS.success + '44',
  },
  countBadgeText: { color: COLORS.success, fontSize: SIZES.xs, fontWeight: '700' },
  card: {
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radius,
    padding: SIZES.padding,
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  iconBox: {
    width: 48,
    height: 48,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  info: { flex: 1 },
  nombre: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '700' },
  descripcion: { color: COLORS.textSecondary, fontSize: SIZES.sm, marginTop: 3 },
  priceCol: { alignItems: 'flex-end', gap: 4 },
  precio: { color: COLORS.primary, fontSize: SIZES.lg, fontWeight: '800' },
  empty: { alignItems: 'center', justifyContent: 'center', paddingTop: 60 },
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
  emptySub: {
    color: COLORS.textSecondary,
    fontSize: SIZES.md,
    marginTop: 6,
    textAlign: 'center',
    maxWidth: 240,
  },
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
