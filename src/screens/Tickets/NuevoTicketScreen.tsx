import React, { useState } from 'react';
import {
  View, Text, TouchableOpacity,
  StyleSheet, ScrollView, FlatList, Alert,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';
import { useAppStore } from '../../store/useAppStore';
import { Item, Producto } from '../../types';

export const NuevoTicketScreen = ({ navigation }: any) => {
  const { productos, createTicket, empresa, configuracion } = useAppStore();

  const [items, setItems] = useState<Item[]>([]);
  const [showProductPicker, setShowProductPicker] = useState(false);

  const addItem = (producto: Producto) => {
    const existing = items.find(i => i.productoId === producto.id);
    if (existing) {
      setItems(items.map(i =>
        i.productoId === producto.id
          ? { ...i, cantidad: i.cantidad + 1, subtotal: (i.cantidad + 1) * i.precio }
          : i,
      ));
    } else {
      setItems([...items, {
        productoId: producto.id,
        nombre: producto.nombre,
        cantidad: 1,
        precio: producto.precio,
        subtotal: producto.precio,
      }]);
    }
    setShowProductPicker(false);
  };

  const removeItem = (productoId: number) => {
    setItems(items.filter(i => i.productoId !== productoId));
  };

  const calculateTotal = () => items.reduce((acc, i) => acc + i.subtotal, 0);

  const handleSave = async () => {
    if (items.length === 0) {
      Alert.alert('Sin productos', 'Agrega al menos un producto al ticket');
      return;
    }

    const ticketData = {
      numero: `${configuracion?.prefijoTicket || 'TKT-'}${Date.now().toString().slice(-6)}`,
      empresaId: empresa?.id || 1,
      fecha: new Date().toLocaleDateString(),
      total: calculateTotal(),
      notas: '',
      items,
    };

    try {
      const id = await createTicket(ticketData);
      Alert.alert('Ticket generado', '¡Listo!', [
        { text: 'Ver ticket', onPress: () => navigation.navigate('DetalleTicket', { id }) },
      ]);
    } catch (e: any) {
      console.error('[NuevoTicket] handleSave:', e);
      Alert.alert('Error al guardar', e?.message ?? 'No se pudo guardar el ticket.');
    }
  };

  return (
    <View style={styles.container}>
      <ScrollView style={styles.scroll} showsVerticalScrollIndicator={false}>

        {/* Encabezado de productos */}
        <View style={styles.sectionRow}>
          <Text style={styles.sectionTitle}>Productos en venta</Text>
          <TouchableOpacity
            onPress={() => setShowProductPicker(true)}
            style={styles.addBtn}
            activeOpacity={0.75}
          >
            <Icon name="plus" size={14} color={COLORS.secondary} />
            <Text style={styles.addBtnText}>Agregar</Text>
          </TouchableOpacity>
        </View>

        {items.length === 0 ? (
          <TouchableOpacity
            style={styles.emptyItemsBox}
            onPress={() => setShowProductPicker(true)}
            activeOpacity={0.7}
          >
            <Icon name="ticket-confirmation-outline" size={28} color={COLORS.textMuted} />
            <Text style={styles.emptyItemsText}>Toca para agregar productos</Text>
          </TouchableOpacity>
        ) : (
          items.map((item, index) => (
            <View key={index} style={styles.itemRow}>
              <View style={styles.itemQtyBox}>
                <Text style={styles.itemQty}>{item.cantidad}x</Text>
              </View>
              <View style={styles.itemInfo}>
                <Text style={styles.itemName}>{item.nombre}</Text>
                <Text style={styles.itemPrice}>${item.precio.toFixed(2)} c/u</Text>
              </View>
              <Text style={styles.itemTotal}>${item.subtotal.toFixed(2)}</Text>
              <TouchableOpacity onPress={() => removeItem(item.productoId!)} style={styles.removeBtn}>
                <Icon name="close-circle" size={18} color={COLORS.textMuted} />
              </TouchableOpacity>
            </View>
          ))
        )}
      </ScrollView>

      {/* Footer con total */}
      <View style={styles.footer}>
        <View style={styles.totalRow}>
          <Text style={styles.totalLabel}>Total a cobrar</Text>
          <Text style={styles.totalValue}>${calculateTotal().toFixed(2)}</Text>
        </View>
        <TouchableOpacity onPress={handleSave} activeOpacity={0.8}>
          <LinearGradient
            colors={GRADIENTS.secondary}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 0 }}
            style={[styles.saveBtn, SHADOWS.md]}
          >
            <Icon name="check-circle-outline" size={20} color={COLORS.white} style={{ marginRight: 8 }} />
            <Text style={styles.saveBtnText}>Finalizar ticket</Text>
          </LinearGradient>
        </TouchableOpacity>
      </View>

      {/* Modal Selector de Productos */}
      {showProductPicker && (
        <View style={styles.modalOverlay}>
          <TouchableOpacity style={styles.modalBackdrop} onPress={() => setShowProductPicker(false)} />
          <View style={styles.modalContent}>
            <View style={styles.modalHandle} />
            <Text style={styles.modalTitle}>Seleccionar producto</Text>
            <FlatList
              data={productos}
              keyExtractor={p => String(p.id)}
              showsVerticalScrollIndicator={false}
              renderItem={({ item }) => (
                <TouchableOpacity
                  style={styles.pickerItem}
                  onPress={() => addItem(item)}
                  activeOpacity={0.7}
                >
                  <View style={styles.pickerIconBox}>
                    <Icon name="package-variant-closed" size={18} color={COLORS.secondary} />
                  </View>
                  <View style={styles.pickerInfo}>
                    <Text style={styles.pickerName}>{item.nombre}</Text>
                    {item.descripcion ? (
                      <Text style={styles.pickerDesc} numberOfLines={1}>{item.descripcion}</Text>
                    ) : null}
                  </View>
                  <View style={styles.pickerRight}>
                    <Text style={styles.pickerPrice}>${item.precio.toFixed(2)}</Text>
                    <Icon name="plus-circle-outline" size={22} color={COLORS.secondary} />
                  </View>
                </TouchableOpacity>
              )}
              ListEmptyComponent={
                <View style={styles.pickerEmpty}>
                  <Icon name="package-variant-closed-outline" size={36} color={COLORS.textMuted} />
                  <Text style={styles.pickerEmptyText}>No hay productos disponibles</Text>
                </View>
              }
            />
            <TouchableOpacity style={styles.closeBtn} onPress={() => setShowProductPicker(false)}>
              <Text style={styles.closeBtnText}>Cancelar</Text>
            </TouchableOpacity>
          </View>
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  scroll: { flex: 1, padding: SIZES.padding },
  sectionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
    marginTop: 6,
  },
  sectionTitle: {
    color: COLORS.textSecondary,
    fontSize: SIZES.xs,
    fontWeight: '700',
    letterSpacing: 1.2,
    textTransform: 'uppercase',
  },
  addBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: COLORS.secondary + '20',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: SIZES.radiusFull,
    borderWidth: 1,
    borderColor: COLORS.secondary + '44',
  },
  addBtnText: { color: COLORS.secondary, fontSize: SIZES.sm, fontWeight: '700' },
  emptyItemsBox: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 40,
    borderRadius: SIZES.radius,
    borderWidth: 1.5,
    borderColor: COLORS.border,
    borderStyle: 'dashed',
    gap: 10,
    marginTop: 10,
  },
  emptyItemsText: { color: COLORS.textMuted, fontSize: SIZES.sm },
  itemRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radiusSm,
    padding: 12,
    marginBottom: 8,
    borderWidth: 1,
    borderColor: COLORS.border,
    gap: 10,
  },
  itemQtyBox: {
    backgroundColor: COLORS.secondary + '22',
    borderRadius: 8,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  itemQty: { color: COLORS.secondary, fontSize: SIZES.sm, fontWeight: '700' },
  itemInfo: { flex: 1 },
  itemName: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '600' },
  itemPrice: { color: COLORS.textSecondary, fontSize: SIZES.xs, marginTop: 2 },
  itemTotal: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '700' },
  removeBtn: { padding: 4 },

  // Footer
  footer: {
    padding: SIZES.padding,
    backgroundColor: COLORS.backgroundCard,
    borderTopWidth: 1,
    borderTopColor: COLORS.border,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
  },
  totalRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },
  totalLabel: { color: COLORS.textSecondary, fontSize: SIZES.sm, fontWeight: '600' },
  totalValue: { color: COLORS.secondary, fontSize: SIZES.xxl, fontWeight: '800' },
  saveBtn: {
    borderRadius: SIZES.radiusLg,
    paddingVertical: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  saveBtnText: { color: COLORS.white, fontSize: SIZES.lg, fontWeight: '700', letterSpacing: 0.3 },

  // Modal
  modalOverlay: {
    position: 'absolute',
    top: 0, left: 0, right: 0, bottom: 0,
    justifyContent: 'flex-end',
  },
  modalBackdrop: {
    position: 'absolute',
    top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.6)',
  },
  modalContent: {
    backgroundColor: COLORS.surface,
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    padding: SIZES.paddingLg,
    maxHeight: '75%',
    borderTopWidth: 1,
    borderColor: COLORS.border,
  },
  modalHandle: {
    width: 40,
    height: 4,
    backgroundColor: COLORS.borderLight,
    borderRadius: 2,
    alignSelf: 'center',
    marginBottom: 16,
  },
  modalTitle: {
    color: COLORS.textPrimary,
    fontSize: SIZES.lg,
    fontWeight: '800',
    textAlign: 'center',
    marginBottom: 16,
  },
  pickerItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
    gap: 12,
  },
  pickerIconBox: {
    width: 40,
    height: 40,
    borderRadius: 10,
    backgroundColor: COLORS.secondary + '20',
    alignItems: 'center',
    justifyContent: 'center',
  },
  pickerInfo: { flex: 1 },
  pickerName: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '600' },
  pickerDesc: { color: COLORS.textSecondary, fontSize: SIZES.sm, marginTop: 2 },
  pickerRight: { alignItems: 'flex-end', gap: 4 },
  pickerPrice: { color: COLORS.secondary, fontSize: SIZES.base, fontWeight: '800' },
  pickerEmpty: { alignItems: 'center', paddingVertical: 40, gap: 10 },
  pickerEmptyText: { color: COLORS.textSecondary, fontSize: SIZES.base },
  closeBtn: {
    marginTop: 16,
    paddingVertical: 14,
    borderRadius: SIZES.radiusLg,
    alignItems: 'center',
    backgroundColor: COLORS.backgroundCard,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  closeBtnText: { color: COLORS.textSecondary, fontSize: SIZES.base, fontWeight: '600' },
});
