import React from 'react';
import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity, Alert,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';
import { useAppStore } from '../../store/useAppStore';
import { pdfService } from '../../services/pdfService';
import { shareService } from '../../services/shareService';

const estadoConfig: Record<string, { color: string; icon: string; label: string }> = {
  pagada:    { color: COLORS.success, icon: 'check-circle',  label: 'Pagada' },
  pendiente: { color: COLORS.warning, icon: 'clock-outline', label: 'Pendiente' },
  cancelada: { color: COLORS.error,   icon: 'close-circle',  label: 'Cancelada' },
};

const InfoRow = ({ label, value }: { label: string; value: string }) => (
  <View style={styles.infoRow}>
    <Text style={styles.infoLabel}>{label}</Text>
    <Text style={styles.infoValue}>{value}</Text>
  </View>
);

// ─── Botón de acción ───────────────────────────────────────────────────────────

const ActionBtn = ({
  iconName, label, colors, onPress,
}: {
  iconName: string;
  label: string;
  colors: string[];
  onPress: () => void;
}) => (
  <TouchableOpacity style={styles.actionBtnWrapper} onPress={onPress} activeOpacity={0.8}>
    <LinearGradient colors={colors} style={[styles.actionBtn, SHADOWS.sm]}>
      <Icon name={iconName} size={20} color={COLORS.white} />
      <Text style={styles.actionBtnText}>{label}</Text>
    </LinearGradient>
  </TouchableOpacity>
);

// ─── Pantalla ─────────────────────────────────────────────────────────────────

export const DetalleFacturaScreen = ({ route }: any) => {
  const { facturas, empresa, configuracion } = useAppStore();
  const id = route.params?.id;
  const factura = facturas.find(f => f.id === id);

  if (!id || !factura) {
    return (
      <View style={styles.center}>
        <Icon name="file-document-alert-outline" size={56} color={COLORS.textMuted} />
        <Text style={styles.notFoundText}>Factura no encontrada</Text>
      </View>
    );
  }

  const cfg = estadoConfig[factura.estado] ?? estadoConfig.pendiente;

  // ── Handlers ────────────────────────────────────────────────────────────

  const handleImprimir = async () => {
    try {
      await pdfService.generateFacturaPDF(factura, empresa, configuracion);
    } catch {
      Alert.alert('Error', 'No se pudo generar el PDF');
    }
  };

  const handleWhatsApp = async () => {
    try {
      await shareService.facturaWhatsApp(factura, empresa, configuracion);
    } catch (e: any) {
      Alert.alert('Error', e?.message ?? 'No se pudo abrir WhatsApp');
    }
  };

  const handleCorreo = async () => {
    try {
      await shareService.facturaCorreo(factura, empresa, configuracion);
    } catch (e: any) {
      Alert.alert('Error', e?.message ?? 'No se pudo abrir el correo');
    }
  };

  const handleCompartir = async () => {
    try {
      await shareService.facturaCompartir(factura, empresa, configuracion);
    } catch (e: any) {
      Alert.alert('Error', e?.message ?? 'No se pudo compartir');
    }
  };

  // ── Render ───────────────────────────────────────────────────────────────

  return (
    <View style={styles.container}>
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>

        {/* Estado */}
        <View style={[styles.estadoBanner, { backgroundColor: cfg.color + '18', borderColor: cfg.color + '44' }]}>
          <Icon name={cfg.icon} size={18} color={cfg.color} />
          <Text style={[styles.estadoText, { color: cfg.color }]}>{cfg.label}</Text>
        </View>

        {/* Tarjeta principal */}
        <View style={[styles.card, SHADOWS.sm]}>
          <View style={styles.cardHeader}>
            <View>
              <Text style={styles.numero}>{factura.numero}</Text>
              <Text style={styles.empresaNombre}>{empresa?.nombre ?? 'Mi Negocio'}</Text>
            </View>
            <LinearGradient colors={GRADIENTS.primary} style={styles.docIcon}>
              <Icon name="file-document-outline" size={22} color={COLORS.white} />
            </LinearGradient>
          </View>

          <View style={styles.divider} />
          <InfoRow label="Fecha"   value={factura.fecha} />
          <InfoRow label="Cliente" value={factura.clienteNombre || 'Consumidor Final'} />
          <View style={styles.divider} />

          {/* Items */}
          <Text style={styles.itemsTitle}>Detalle de productos</Text>
          {factura.items.map((item, idx) => (
            <View key={idx} style={styles.itemRow}>
              <View style={styles.itemQtyBox}>
                <Text style={styles.itemQty}>{item.cantidad}x</Text>
              </View>
              <Text style={styles.itemName} numberOfLines={1}>{item.nombre}</Text>
              <Text style={styles.itemTotal}>${item.subtotal.toFixed(2)}</Text>
            </View>
          ))}

          <View style={styles.divider} />

          {/* Totales */}
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Subtotal</Text>
            <Text style={styles.summaryValue}>${factura.subtotal.toFixed(2)}</Text>
          </View>
          {factura.impuesto > 0 && (
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Impuesto</Text>
              <Text style={styles.summaryValue}>${factura.impuesto.toFixed(2)}</Text>
            </View>
          )}
          <View style={[styles.summaryRow, styles.totalRowHighlight]}>
            <Text style={styles.totalLabel}>TOTAL</Text>
            <Text style={styles.totalValue}>${factura.total.toFixed(2)}</Text>
          </View>
        </View>

        {/* ── Acciones de envío ── */}
        <Text style={styles.accionesTitle}>Enviar factura</Text>
        <View style={styles.actionsGrid}>
          <ActionBtn
            iconName="whatsapp"
            label="WhatsApp"
            colors={['#25D366', '#128C7E']}
            onPress={handleWhatsApp}
          />
          <ActionBtn
            iconName="email-outline"
            label="Correo"
            colors={['#EA4335', '#C23120']}
            onPress={handleCorreo}
          />
          <ActionBtn
            iconName="share-variant-outline"
            label="Compartir"
            colors={GRADIENTS.primary}
            onPress={handleCompartir}
          />
          <ActionBtn
            iconName="printer-outline"
            label="Imprimir"
            colors={['#444466', '#222233']}
            onPress={handleImprimir}
          />
        </View>

      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 12 },
  notFoundText: { color: COLORS.textSecondary, fontSize: SIZES.lg },
  content: { padding: SIZES.padding, paddingBottom: 40 },

  estadoBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: SIZES.radiusFull,
    borderWidth: 1,
    alignSelf: 'flex-start',
    marginBottom: 14,
  },
  estadoText: { fontSize: SIZES.sm, fontWeight: '700', textTransform: 'capitalize' },

  card: {
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radiusLg,
    padding: SIZES.paddingLg,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 16,
  },
  numero: { color: COLORS.textPrimary, fontSize: SIZES.h3, fontWeight: '800', letterSpacing: -0.5 },
  empresaNombre: { color: COLORS.textSecondary, fontSize: SIZES.sm, marginTop: 3 },
  docIcon: {
    width: 48, height: 48, borderRadius: 14,
    alignItems: 'center', justifyContent: 'center',
  },
  divider: { height: 1, backgroundColor: COLORS.border, marginVertical: 14 },
  infoRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 },
  infoLabel: { color: COLORS.textMuted, fontSize: SIZES.sm },
  infoValue: { color: COLORS.textPrimary, fontSize: SIZES.sm, fontWeight: '600' },

  itemsTitle: {
    color: COLORS.textSecondary,
    fontSize: SIZES.xs,
    fontWeight: '700',
    letterSpacing: 1.1,
    textTransform: 'uppercase',
    marginBottom: 12,
  },
  itemRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 10, gap: 10 },
  itemQtyBox: {
    backgroundColor: COLORS.primary + '22',
    borderRadius: 6,
    paddingHorizontal: 7,
    paddingVertical: 3,
  },
  itemQty: { color: COLORS.primary, fontSize: SIZES.xs, fontWeight: '700' },
  itemName: { flex: 1, color: COLORS.textPrimary, fontSize: SIZES.sm },
  itemTotal: { color: COLORS.textPrimary, fontSize: SIZES.sm, fontWeight: '700' },

  summaryRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 },
  summaryLabel: { color: COLORS.textSecondary, fontSize: SIZES.sm },
  summaryValue: { color: COLORS.textPrimary, fontSize: SIZES.sm, fontWeight: '600' },
  totalRowHighlight: {
    backgroundColor: COLORS.primary + '12',
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderRadius: SIZES.radiusSm,
    marginTop: 4,
  },
  totalLabel: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '800' },
  totalValue: { color: COLORS.primary, fontSize: SIZES.xxl, fontWeight: '800' },

  // Acciones
  accionesTitle: {
    color: COLORS.textSecondary,
    fontSize: SIZES.xs,
    fontWeight: '700',
    letterSpacing: 1.2,
    textTransform: 'uppercase',
    marginTop: SIZES.marginLg,
    marginBottom: 12,
  },
  actionsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
  },
  actionBtnWrapper: { width: '47%' },
  actionBtn: {
    borderRadius: SIZES.radius,
    paddingVertical: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  actionBtnText: { color: COLORS.white, fontSize: SIZES.sm, fontWeight: '700' },
});
