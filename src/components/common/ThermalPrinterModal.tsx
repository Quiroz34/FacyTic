import React, { useState, useCallback } from 'react';
import {
  View, Text, StyleSheet, TouchableOpacity,
  FlatList, ActivityIndicator, Modal, Alert,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import {
  thermalPrinterService,
  BluetoothDevice,
  AnchoImpresora,
} from '../../services/thermalPrinterService';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';

interface Props {
  visible: boolean;
  onClose: () => void;
  onImprimir: (macAddress: string, ancho: AnchoImpresora) => Promise<void>;
}

const ANCHOS: { label: string; value: AnchoImpresora; desc: string }[] = [
  { label: '58 mm', value: 58, desc: 'Tickets pequeños' },
  { label: '80 mm', value: 80, desc: 'Facturas / recibos' },
];

export const ThermalPrinterModal = ({ visible, onClose, onImprimir }: Props) => {
  const [dispositivos, setDispositivos] = useState<BluetoothDevice[]>([]);
  const [buscando, setBuscando]         = useState(false);
  const [imprimiendo, setImprimiendo]   = useState(false);
  const [seleccionado, setSeleccionado] = useState<BluetoothDevice | null>(null);
  const [ancho, setAncho]               = useState<AnchoImpresora>(58);

  const buscarDispositivos = useCallback(async () => {
    setBuscando(true);
    setDispositivos([]);
    try {
      const lista = await thermalPrinterService.obtenerDispositivos();
      if (lista.length === 0) {
        Alert.alert(
          'Sin impresoras',
          'No se encontraron impresoras Bluetooth emparejadas.\n\nAsegúrate de que la impresora esté encendida y vinculada en Ajustes → Bluetooth.',
        );
      }
      setDispositivos(lista);
    } catch (e: any) {
      Alert.alert('Error Bluetooth', e?.message ?? 'No se pudo acceder al Bluetooth. Verifica que esté activado.');
    } finally {
      setBuscando(false);
    }
  }, []);

  const handleImprimir = async () => {
    if (!seleccionado) {
      Alert.alert('Selecciona una impresora', 'Toca una impresora de la lista para seleccionarla.');
      return;
    }
    setImprimiendo(true);
    try {
      await onImprimir(seleccionado.macAddress, ancho);
      Alert.alert('¡Impreso!', `Enviado a ${seleccionado.deviceName}`);
      onClose();
    } catch (e: any) {
      Alert.alert('Error al imprimir', e?.message ?? 'No se pudo enviar el trabajo de impresión.');
    } finally {
      setImprimiendo(false);
    }
  };

  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent
      onRequestClose={onClose}
    >
      <View style={styles.overlay}>
        <TouchableOpacity style={styles.backdrop} onPress={onClose} />

        <View style={styles.sheet}>
          {/* Handle */}
          <View style={styles.handle} />

          {/* Header */}
          <View style={styles.header}>
            <View style={styles.headerLeft}>
              <Icon name="printer-wireless" size={22} color={COLORS.primary} />
              <Text style={styles.title}>Impresora térmica</Text>
            </View>
            <TouchableOpacity onPress={onClose} style={styles.closeBtn}>
              <Icon name="close" size={20} color={COLORS.textMuted} />
            </TouchableOpacity>
          </View>

          {/* Ancho de papel */}
          <Text style={styles.sectionLabel}>Ancho del papel</Text>
          <View style={styles.anchoRow}>
            {ANCHOS.map(a => (
              <TouchableOpacity
                key={a.value}
                onPress={() => setAncho(a.value)}
                style={[styles.anchoBtn, ancho === a.value && styles.anchoBtnActive]}
                activeOpacity={0.75}
              >
                <Text style={[styles.anchoBtnLabel, ancho === a.value && styles.anchoBtnLabelActive]}>
                  {a.label}
                </Text>
                <Text style={[styles.anchoBtnDesc, ancho === a.value && styles.anchoBtnDescActive]}>
                  {a.desc}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          {/* Buscar */}
          <Text style={styles.sectionLabel}>Impresoras Bluetooth</Text>
          <TouchableOpacity
            onPress={buscarDispositivos}
            style={styles.buscarBtn}
            disabled={buscando}
            activeOpacity={0.75}
          >
            {buscando ? (
              <ActivityIndicator size="small" color={COLORS.primary} />
            ) : (
              <Icon name="bluetooth-search" size={18} color={COLORS.primary} />
            )}
            <Text style={styles.buscarBtnText}>
              {buscando ? 'Buscando...' : 'Buscar impresoras'}
            </Text>
          </TouchableOpacity>

          {/* Lista de dispositivos */}
          <FlatList
            data={dispositivos}
            keyExtractor={d => d.macAddress}
            style={styles.lista}
            showsVerticalScrollIndicator={false}
            ListEmptyComponent={
              !buscando ? (
                <View style={styles.listaVacia}>
                  <Icon name="printer-off-outline" size={36} color={COLORS.textMuted} />
                  <Text style={styles.listaVaciaText}>
                    {dispositivos.length === 0
                      ? 'Toca "Buscar impresoras" para comenzar'
                      : 'No se encontraron dispositivos'}
                  </Text>
                </View>
              ) : null
            }
            renderItem={({ item }) => {
              const isSelected = seleccionado?.macAddress === item.macAddress;
              return (
                <TouchableOpacity
                  style={[styles.dispositivo, isSelected && styles.dispositivoSeleccionado]}
                  onPress={() => setSeleccionado(item)}
                  activeOpacity={0.75}
                >
                  <View style={[styles.dispositivoIcon, isSelected && styles.dispositivoIconActive]}>
                    <Icon
                      name="printer-pos-outline"
                      size={20}
                      color={isSelected ? COLORS.white : COLORS.primary}
                    />
                  </View>
                  <View style={styles.dispositivoInfo}>
                    <Text style={[styles.dispositivoNombre, isSelected && styles.dispositivoNombreActive]}>
                      {item.deviceName || 'Impresora desconocida'}
                    </Text>
                    <Text style={styles.dispositivoMac}>{item.macAddress}</Text>
                  </View>
                  {isSelected && (
                    <Icon name="check-circle" size={20} color={COLORS.primary} />
                  )}
                </TouchableOpacity>
              );
            }}
          />

          {/* Botón imprimir */}
          <TouchableOpacity
            onPress={handleImprimir}
            disabled={!seleccionado || imprimiendo}
            activeOpacity={0.8}
            style={[(!seleccionado || imprimiendo) && styles.btnDisabled]}
          >
            <LinearGradient
              colors={GRADIENTS.primary}
              start={{ x: 0, y: 0 }}
              end={{ x: 1, y: 0 }}
              style={[styles.btnImprimir, SHADOWS.md]}
            >
              {imprimiendo ? (
                <ActivityIndicator color={COLORS.white} size="small" />
              ) : (
                <>
                  <Icon name="printer-check" size={20} color={COLORS.white} />
                  <Text style={styles.btnImprimirText}>Imprimir ahora</Text>
                </>
              )}
            </LinearGradient>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: { flex: 1, justifyContent: 'flex-end' },
  backdrop: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)' },
  sheet: {
    backgroundColor: COLORS.surface,
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    padding: SIZES.paddingLg,
    maxHeight: '85%',
    borderTopWidth: 1,
    borderColor: COLORS.border,
  },
  handle: {
    width: 40, height: 4,
    backgroundColor: COLORS.borderLight,
    borderRadius: 2,
    alignSelf: 'center',
    marginBottom: 16,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  headerLeft: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  title: { color: COLORS.textPrimary, fontSize: SIZES.lg, fontWeight: '800' },
  closeBtn: {
    width: 32, height: 32, borderRadius: 16,
    backgroundColor: COLORS.backgroundCard,
    alignItems: 'center', justifyContent: 'center',
  },

  sectionLabel: {
    color: COLORS.textSecondary,
    fontSize: SIZES.xs,
    fontWeight: '700',
    letterSpacing: 1.1,
    textTransform: 'uppercase',
    marginBottom: 10,
  },

  // Ancho de papel
  anchoRow: { flexDirection: 'row', gap: 10, marginBottom: 20 },
  anchoBtn: {
    flex: 1,
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radiusSm,
    padding: 12,
    alignItems: 'center',
    borderWidth: 1.5,
    borderColor: COLORS.border,
  },
  anchoBtnActive: { borderColor: COLORS.primary, backgroundColor: COLORS.primary + '18' },
  anchoBtnLabel: { color: COLORS.textSecondary, fontSize: SIZES.base, fontWeight: '700' },
  anchoBtnLabelActive: { color: COLORS.primary },
  anchoBtnDesc: { color: COLORS.textMuted, fontSize: SIZES.xs, marginTop: 3 },
  anchoBtnDescActive: { color: COLORS.primary + 'AA' },

  // Buscar
  buscarBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    backgroundColor: COLORS.primary + '18',
    borderRadius: SIZES.radiusSm,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderWidth: 1,
    borderColor: COLORS.primary + '44',
    marginBottom: 12,
  },
  buscarBtnText: { color: COLORS.primary, fontSize: SIZES.base, fontWeight: '600' },

  // Lista
  lista: { maxHeight: 200, marginBottom: 16 },
  listaVacia: { alignItems: 'center', paddingVertical: 24, gap: 8 },
  listaVaciaText: {
    color: COLORS.textMuted,
    fontSize: SIZES.sm,
    textAlign: 'center',
    maxWidth: 240,
  },
  dispositivo: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radiusSm,
    padding: 12,
    marginBottom: 8,
    borderWidth: 1.5,
    borderColor: COLORS.border,
    gap: 12,
  },
  dispositivoSeleccionado: {
    borderColor: COLORS.primary,
    backgroundColor: COLORS.primary + '10',
  },
  dispositivoIcon: {
    width: 40, height: 40, borderRadius: 10,
    backgroundColor: COLORS.primary + '20',
    alignItems: 'center', justifyContent: 'center',
  },
  dispositivoIconActive: { backgroundColor: COLORS.primary },
  dispositivoInfo: { flex: 1 },
  dispositivoNombre: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '600' },
  dispositivoNombreActive: { color: COLORS.primary },
  dispositivoMac: { color: COLORS.textMuted, fontSize: SIZES.xs, marginTop: 2 },

  // Botón imprimir
  btnImprimir: {
    borderRadius: SIZES.radiusLg,
    paddingVertical: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
  },
  btnImprimirText: { color: COLORS.white, fontSize: SIZES.lg, fontWeight: '700' },
  btnDisabled: { opacity: 0.45 },
});
