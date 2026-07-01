import React, { useState } from 'react';
import {
  View, Text, TouchableOpacity,
  StyleSheet, ScrollView, Alert,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';
import { useAppStore } from '../../store/useAppStore';
import { Input } from '../../components/common/Input';

export const NuevoProductoScreen = ({ navigation, route }: any) => {
  const { createProducto, updateProducto, productos } = useAppStore();
  const editId = route.params?.id;
  const isEditing = !!editId;

  const existingProduct = isEditing ? productos.find(p => p.id === editId) : null;

  const [nombre, setNombre] = useState(existingProduct?.nombre || '');
  const [precio, setPrecio] = useState(existingProduct?.precio?.toString() || '');
  const [descripcion, setDescripcion] = useState(existingProduct?.descripcion || '');

  const handleSave = () => {
    if (!nombre.trim() || !precio) {
      Alert.alert('Campos requeridos', 'Nombre y precio son obligatorios');
      return;
    }

    const data = {
      nombre: nombre.trim(),
      precio: parseFloat(precio),
      descripcion: descripcion.trim(),
      activo: 1,
    };

    try {
      if (isEditing) {
        updateProducto(editId, data);
        Alert.alert('Actualizado', 'El producto fue actualizado correctamente');
      } else {
        createProducto(data);
        Alert.alert('Creado', 'El producto fue agregado al catálogo');
      }
      navigation.goBack();
    } catch (e: any) {
      console.error('[NuevoProducto] handleSave:', e);
      Alert.alert('Error al guardar', e?.message ?? 'No se pudo guardar el producto.');
    }
  };

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
    >
      {/* Icono de producto */}
      <View style={styles.iconSection}>
        <LinearGradient colors={GRADIENTS.success} style={[styles.iconCircle, SHADOWS.md]}>
          <Icon name="package-variant-closed" size={36} color={COLORS.white} />
        </LinearGradient>
        <Text style={styles.iconLabel}>
          {isEditing ? 'Editar producto' : 'Nuevo producto'}
        </Text>
      </View>

      {/* Formulario */}
      <View style={styles.card}>
        <Input
          label="Nombre del producto *"
          iconName="tag-outline"
          placeholder="Ej. Coca Cola 350ml"
          value={nombre}
          onChangeText={setNombre}
        />
        <Input
          label="Precio *"
          iconName="currency-usd"
          placeholder="0.00"
          keyboardType="numeric"
          value={precio}
          onChangeText={setPrecio}
        />
        <Input
          label="Descripción (opcional)"
          iconName="text-box-outline"
          placeholder="Detalles del producto..."
          multiline
          numberOfLines={4}
          value={descripcion}
          onChangeText={setDescripcion}
          style={styles.textArea}
        />
      </View>

      {/* Botón guardar */}
      <TouchableOpacity onPress={handleSave} activeOpacity={0.8} style={SHADOWS.md}>
        <LinearGradient
          colors={isEditing ? GRADIENTS.primary : GRADIENTS.success}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 0 }}
          style={styles.button}
        >
          <Icon
            name={isEditing ? 'content-save-outline' : 'plus-circle-outline'}
            size={20}
            color={COLORS.white}
            style={{ marginRight: 8 }}
          />
          <Text style={styles.buttonText}>
            {isEditing ? 'Actualizar producto' : 'Guardar producto'}
          </Text>
        </LinearGradient>
      </TouchableOpacity>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  content: { padding: SIZES.padding, paddingBottom: 60 },
  iconSection: { alignItems: 'center', paddingVertical: SIZES.paddingLg },
  iconCircle: {
    width: 80,
    height: 80,
    borderRadius: 40,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 12,
  },
  iconLabel: { color: COLORS.textSecondary, fontSize: SIZES.sm, fontWeight: '600' },
  card: {
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radiusLg,
    padding: SIZES.padding,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  textArea: { height: 90, textAlignVertical: 'top' },
  button: {
    borderRadius: SIZES.radiusLg,
    paddingVertical: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonText: { color: COLORS.white, fontSize: SIZES.lg, fontWeight: '700', letterSpacing: 0.3 },
});
