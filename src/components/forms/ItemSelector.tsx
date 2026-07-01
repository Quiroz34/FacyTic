import React, { useState } from 'react';
import {
  View, Text, TouchableOpacity, FlatList,
  StyleSheet, Modal, TextInput,
} from 'react-native';
import { COLORS, SIZES, SHADOWS } from '../../theme';
import { Item, Producto } from '../../types';

interface Props {
  productos: Producto[];
  items: Item[];
  onAdd: (item: Item) => void;
  onRemove: (index: number) => void;
  onChangeQty: (index: number, qty: number) => void;
}

export const ItemSelector = ({ productos, items, onAdd, onRemove, onChangeQty }: Props) => {
  const [modal, setModal] = useState(false);
  const [search, setSearch] = useState('');
  const [manualNombre, setManualNombre] = useState('');
  const [manualPrecio, setManualPrecio] = useState('');

  const filtrados = productos.filter(p =>
    p.nombre.toLowerCase().includes(search.toLowerCase()),
  );

  const agregarProducto = (p: Producto) => {
    onAdd({
      productoId: p.id,
      nombre: p.nombre,
      descripcion: p.descripcion,
      cantidad: 1,
      precio: p.precio,
      subtotal: p.precio,
    });
    setModal(false);
    setSearch('');
  };

  const agregarManual = () => {
    if (!manualNombre.trim() || !manualPrecio.trim()) return;
    const precio = parseFloat(manualPrecio) || 0;
    onAdd({
      nombre: manualNombre.trim(),
      cantidad: 1,
      precio,
      subtotal: precio,
    });
    setManualNombre('');
    setManualPrecio('');
    setModal(false);
  };

  return (
    <View>
      {/* Lista de items agregados */}
      {items.map((item, i) => (
        <View key={i} style={[styles.itemRow, SHADOWS.sm]}>
          <View style={styles.itemInfo}>
            <Text style={styles.itemNombre}>{item.nombre}</Text>
            <Text style={styles.itemPrecio}>${item.precio.toFixed(2)} c/u</Text>
          </View>
          <View style={styles.itemControls}>
            <TouchableOpacity
              style={styles.qtyBtn}
              onPress={() => item.cantidad > 1 && onChangeQty(i, item.cantidad - 1)}
            >
              <Text style={styles.qtyBtnText}>−</Text>
            </TouchableOpacity>
            <Text style={styles.qty}>{item.cantidad}</Text>
            <TouchableOpacity
              style={styles.qtyBtn}
              onPress={() => onChangeQty(i, item.cantidad + 1)}
            >
              <Text style={styles.qtyBtnText}>+</Text>
            </TouchableOpacity>
            <Text style={styles.subtotal}>${item.subtotal.toFixed(2)}</Text>
            <TouchableOpacity onPress={() => onRemove(i)} style={styles.removeBtn}>
              <Text style={styles.removeBtnText}>✕</Text>
            </TouchableOpacity>
          </View>
        </View>
      ))}

      {/* Botón agregar */}
      <TouchableOpacity style={styles.addBtn} onPress={() => setModal(true)}>
        <Text style={styles.addBtnText}>+ Agregar producto / servicio</Text>
      </TouchableOpacity>

      {/* Modal selector */}
      <Modal visible={modal} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Seleccionar producto</Text>

            <TextInput
              style={styles.modalSearch}
              placeholder="Buscar..."
              placeholderTextColor={COLORS.textMuted}
              value={search}
              onChangeText={setSearch}
            />

            <FlatList
              data={filtrados}
              keyExtractor={p => String(p.id)}
              style={styles.modalList}
              renderItem={({ item: p }) => (
                <TouchableOpacity style={styles.modalItem} onPress={() => agregarProducto(p)}>
                  <Text style={styles.modalItemNombre}>{p.nombre}</Text>
                  <Text style={styles.modalItemPrecio}>${p.precio.toFixed(2)}</Text>
                </TouchableOpacity>
              )}
              ListEmptyComponent={
                <Text style={styles.noResults}>Sin resultados</Text>
              }
            />

            {/* Item manual */}
            <View style={styles.manualSection}>
              <Text style={styles.manualTitle}>O agregar manualmente</Text>
              <TextInput
                style={styles.manualInput}
                placeholder="Nombre del producto/servicio"
                placeholderTextColor={COLORS.textMuted}
                value={manualNombre}
                onChangeText={setManualNombre}
              />
              <TextInput
                style={styles.manualInput}
                placeholder="Precio"
                placeholderTextColor={COLORS.textMuted}
                value={manualPrecio}
                onChangeText={setManualPrecio}
                keyboardType="decimal-pad"
              />
              <TouchableOpacity style={styles.manualBtn} onPress={agregarManual}>
                <Text style={styles.manualBtnText}>Agregar</Text>
              </TouchableOpacity>
            </View>

            <TouchableOpacity style={styles.cancelBtn} onPress={() => setModal(false)}>
              <Text style={styles.cancelText}>Cancelar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  itemRow: {
    backgroundColor: COLORS.backgroundCard, borderRadius: SIZES.radius,
    padding: SIZES.paddingSm, flexDirection: 'row',
    justifyContent: 'space-between', alignItems: 'center',
    marginBottom: 8, borderWidth: 1, borderColor: COLORS.border,
  },
  itemInfo: { flex: 1 },
  itemNombre: { color: COLORS.textPrimary, fontSize: SIZES.sm, fontWeight: '600' },
  itemPrecio: { color: COLORS.textSecondary, fontSize: SIZES.xs },
  itemControls: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  qtyBtn: {
    width: 28, height: 28, borderRadius: 14,
    backgroundColor: COLORS.backgroundInput, alignItems: 'center', justifyContent: 'center',
    borderWidth: 1, borderColor: COLORS.border,
  },
  qtyBtnText: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '700' },
  qty: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '700', minWidth: 20, textAlign: 'center' },
  subtotal: { color: COLORS.primary, fontSize: SIZES.sm, fontWeight: '700', minWidth: 50, textAlign: 'right' },
  removeBtn: { padding: 4 },
  removeBtnText: { color: COLORS.error, fontSize: SIZES.sm, fontWeight: '700' },
  addBtn: {
    borderRadius: SIZES.radius, borderWidth: 1.5,
    borderColor: COLORS.primary, borderStyle: 'dashed',
    padding: SIZES.padding, alignItems: 'center', marginTop: 4,
  },
  addBtnText: { color: COLORS.primary, fontSize: SIZES.base, fontWeight: '600' },
  modalOverlay: {
    flex: 1, backgroundColor: 'rgba(0,0,0,0.7)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: COLORS.backgroundCard, borderTopLeftRadius: 24,
    borderTopRightRadius: 24, padding: SIZES.paddingLg, maxHeight: '85%',
  },
  modalTitle: {
    color: COLORS.textPrimary, fontSize: SIZES.xl,
    fontWeight: '700', marginBottom: SIZES.margin,
  },
  modalSearch: {
    backgroundColor: COLORS.backgroundInput, color: COLORS.textPrimary,
    borderRadius: SIZES.radiusSm, paddingHorizontal: SIZES.padding,
    paddingVertical: 10, fontSize: SIZES.base,
    borderWidth: 1, borderColor: COLORS.border, marginBottom: 12,
  },
  modalList: { maxHeight: 200 },
  modalItem: {
    flexDirection: 'row', justifyContent: 'space-between',
    paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: COLORS.border,
  },
  modalItemNombre: { color: COLORS.textPrimary, fontSize: SIZES.base },
  modalItemPrecio: { color: COLORS.primary, fontSize: SIZES.base, fontWeight: '700' },
  noResults: { color: COLORS.textMuted, textAlign: 'center', padding: 20 },
  manualSection: { marginTop: 16, paddingTop: 16, borderTopWidth: 1, borderTopColor: COLORS.border },
  manualTitle: { color: COLORS.textSecondary, fontSize: SIZES.sm, marginBottom: 8, fontWeight: '600' },
  manualInput: {
    backgroundColor: COLORS.backgroundInput, color: COLORS.textPrimary,
    borderRadius: SIZES.radiusSm, paddingHorizontal: SIZES.padding,
    paddingVertical: 10, fontSize: SIZES.base,
    borderWidth: 1, borderColor: COLORS.border, marginBottom: 8,
  },
  manualBtn: {
    backgroundColor: COLORS.primary, borderRadius: SIZES.radiusSm,
    padding: 12, alignItems: 'center',
  },
  manualBtnText: { color: COLORS.white, fontWeight: '700', fontSize: SIZES.base },
  cancelBtn: { marginTop: 12, alignItems: 'center', padding: 12 },
  cancelText: { color: COLORS.error, fontSize: SIZES.base, fontWeight: '600' },
});
