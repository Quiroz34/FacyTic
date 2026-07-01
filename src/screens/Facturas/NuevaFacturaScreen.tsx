import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity,
  StyleSheet, ScrollView, FlatList, Alert,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';
import { useAppStore } from '../../store/useAppStore';
import { Item, Producto } from '../../types';

export const NuevaFacturaScreen = ({ navigation }: any) => {
  const { productos, createFactura, empresa, configuracion } = useAppStore();

  const [clienteNombre, setClienteNombre] = useState('');
  const [items, setItems] = useState<Item[]>([]);
  const [showProductPicker, setShowProductPicker] = useState(false);
  const [clienteFocused, setClienteFocused] = useState(false);

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

  const calculateSubtotal = () => items.reduce((acc, i) => acc + i.subtotal, 0);
  const calculateImpuesto = () => calculateSubtotal() * ((configuracion?.impuesto || 0) / 100);
  const calculateTotal = () => calculateSubtotal() + calculateImpuesto();

  const handleSave = async () => {
    if (items.length === 0) {
      Alert.alert('Sin productos', 'Debes agregar al menos un producto a la factura');
      return;
    }

    const subtotal = calculateSubtotal();
    const impuesto = calculateImpuesto();
    const total = subtotal + impuesto;

    const facturaData = {
      numero: `${configuracion?.prefijoFactura || 'FAC-'}${Date.now().toString().slice(-6)}`,
      clienteNombre: clienteNombre.trim() || 'Consumidor Final',
      empresaId: empresa?.id || 1,
      fecha: new Date().toLocaleDateString(),
      subtotal,
      descuento: 0,
      impuesto,
      total,
      estado: 'pendiente' as const,
      items,
    };

    try {
      const id = await createFactura(facturaData);
      Alert.alert('Factura generada', 'La factura fue creada exitosamente', [
        { text: 'Ver factura', onPress: () => navigation.navigate('DetalleFactura', { id }) },
      ]);
    } catch (e: any) {
      console.error('[NuevaFactura] handleSave:', e);
      Alert.alert('Error al guardar', e?.message ?? 'No se pudo generar la factura.');
    }
  };

  return (
    <View style={styles.container}>
      <ScrollView style={styles.scroll} showsVerticalScrollIndicator={false}>

        {/* Cliente */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Datos del cliente</Text>
          <View style={[styles.inputRow, clienteFocused && styles.inputRowFocused]}>
            <Icon name="account-outline" size={18} color={clienteFocused ? COLORS.primary : COLORS.textMuted} style={styles.inputIcon} />
            <TextInput
              style={styles.input}
              placeholder="Nombre del cliente (opcional)"
              placeholderTextColor={COLORS.textMuted}
              value={clienteNombre}
              onChangeText={setClienteNombre}
              onFocus={() => setClienteFocused(true)}
              onBlur={() => setClienteFocused(false)}
            />
          </View>
        </View>

        {/* Productos */}
        <View style={styles.section}>
          <View style={styles.sectionRow}>
            <Text style={styles.sectionTitle}>Productos</Text>
            <TouchableOpacity
              onPress={() => setShowProductPicker(true)}
              style={styles.addBtn}
              activeOpacity={0.75}
            >
              <Icon name="plus" size={14} color={COLORS.primary} />
              <Text style={styles.addBtnText}>Agregar</Text>
            </TouchableOpacity>
          </View>

          {items.length === 0 ? (
            <TouchableOpacity
              style={styles.emptyItemsBox}
              onPress={() => setShowProductPicker(true)}
              activeOpacity={0.7}
            >
              <Icon name="package-variant-closed-outline" size={28} color={COLORS.textMuted} />
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
        </View>

        {/* Resumen */}
        {items.length > 0 && (
          <View style={styles.summaryCard}>
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Subtotal</Text>
              <Text style={styles.summaryValue}>${calculateSubtotal().toFixed(2)}</Text>
            </View>
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>
                {configuracion?.impuestoNombre || 'IVA'} ({configuracion?.impuesto || 0}%)
              </Text>
              <Text style={styles.summaryValue}>${calculateImpuesto().toFixed(2)}</Text>
            </View>
            <View style={styles.divider} />
            <View style={styles.summaryRow}>
              <Text style={styles.totalLabel}>TOTAL</Text>
              <Text style={styles.totalValue}>${calculateTotal().toFixed(2)}</Text>
            </View>
          </View>
        )}
      </ScrollView>

      {/* Footer */}
      <View style={styles.footer}>
        <TouchableOpacity onPress={handleSave} activeOpacity={0.8}>
          <LinearGradient
            colors={GRADIENTS.primary}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 0 }}
            style={[styles.saveBtn, SHADOWS.md]}
          >
            <Icon name="file-document-outline" size={20} color={COLORS.white} style={{ marginRight: 8 }} />
            <Text style={styles.saveBtnText}>Generar Factura</Text>
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
                    <Icon name="package-variant-closed" size={18} color={COLORS.primary} />
                  </View>
                  <View style={styles.pickerInfo}>
                    <Text style={styles.pickerName}>{item.nombre}</Text>
                    {item.descripcion ? (
                      <Text style={styles.pickerDesc} numberOfLines={1}>{item.descripcion}</Text>
                    ) : null}
                  </View>
                  <Text style={styles.pickerPrice}>${item.precio.toFixed(2)}</Text>
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
  section: { marginBottom: 20 },
  sectionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  sectionTitle: {
    color: COLORS.textSecondary,
    fontSize: SIZES.xs,
    fontWeight: '700',
    letterSpacing: 1.2,
    textTransform: 'uppercase',
    marginBottom: 12,
  },
  inputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radiusSm,
    borderWidth: 1.5,
    borderColor: COLORS.border,
    paddingHorizontal: SIZES.padding,
  },
  inputRowFocused: { borderColor: COLORS.primary },
  inputIcon: { marginRight: 10 },
  input: {
    flex: 1,
    color: COLORS.textPrimary,
    paddingVertical: 13,
    fontSize: SIZES.base,
  },
  addBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: COLORS.primary + '20',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: SIZES.radiusFull,
    borderWidth: 1,
    borderColor: COLORS.primary + '44',
  },
  addBtnText: { color: COLORS.primary, fontSize: SIZES.sm, fontWeight: '700' },
  emptyItemsBox: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 30,
    borderRadius: SIZES.radius,
    borderWidth: 1.5,
    borderColor: COLORS.border,
    borderStyle: 'dashed',
    gap: 8,
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
    backgroundColor: COLORS.primary + '22',
    borderRadius: 8,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  itemQty: { color: COLORS.primary, fontSize: SIZES.sm, fontWeight: '700' },
  itemInfo: { flex: 1 },
  itemName: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '600' },
  itemPrice: { color: COLORS.textSecondary, fontSize: SIZES.xs, marginTop: 2 },
  itemTotal: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '700' },
  removeBtn: { padding: 4 },
  summaryCard: {
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radiusLg,
    padding: SIZES.padding,
    borderWidth: 1,
    borderColor: COLORS.border,
    marginBottom: 30,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  summaryLabel: { color: COLORS.textSecondary, fontSize: SIZES.sm },
  summaryValue: { color: COLORS.textPrimary, fontSize: SIZES.sm, fontWeight: '600' },
  divider: { height: 1, backgroundColor: COLORS.border, marginVertical: 10 },
  totalLabel: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '700' },
  totalValue: { color: COLORS.primary, fontSize: SIZES.xl, fontWeight: '800' },
  footer: {
    padding: SIZES.padding,
    borderTopWidth: 1,
    borderTopColor: COLORS.border,
    backgroundColor: COLORS.background,
  },
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
    backgroundColor: COLORS.primary + '20',
    alignItems: 'center',
    justifyContent: 'center',
  },
  pickerInfo: { flex: 1 },
  pickerName: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '600' },
  pickerDesc: { color: COLORS.textSecondary, fontSize: SIZES.sm, marginTop: 2 },
  pickerPrice: { color: COLORS.primary, fontSize: SIZES.base, fontWeight: '800' },
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
