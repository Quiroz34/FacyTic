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

export const DetalleTicketScreen = ({ route }: any) => {
  const { tickets, empresa, configuracion } = useAppStore();
  const id = route.params?.id;
  const ticket = tickets.find(t => t.id === id);

  if (!id || !ticket) {
    return (
      <View style={styles.center}>
        <Icon name="ticket-outline" size={56} color={COLORS.textMuted} />
        <Text style={styles.notFoundText}>Ticket no encontrado</Text>
      </View>
    );
  }

  // ── Handlers ────────────────────────────────────────────────────────────

  const handleImprimir = async () => {
    try {
      await pdfService.generateTicketPDF(ticket, empresa, configuracion);
    } catch {
      Alert.alert('Error', 'No se pudo generar el ticket');
    }
  };

  const handleWhatsApp = async () => {
    try {
      await shareService.ticketWhatsApp(ticket, empresa, configuracion);
    } catch (e: any) {
      Alert.alert('Error', e?.message ?? 'No se pudo abrir WhatsApp');
    }
  };

  const handleCorreo = async () => {
    try {
      await shareService.ticketCorreo(ticket, empresa, configuracion);
    } catch (e: any) {
      Alert.alert('Error', e?.message ?? 'No se pudo abrir el correo');
    }
  };

  const handleCompartir = async () => {
    try {
      await shareService.ticketCompartir(ticket, empresa, configuracion);
    } catch (e: any) {
      Alert.alert('Error', e?.message ?? 'No se pudo compartir');
    }
  };

  // ── Render ───────────────────────────────────────────────────────────────

  return (
    <View style={styles.container}>
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>

        {/* Recibo estilizado */}
        <View style={[styles.receiptCard, SHADOWS.md]}>
          <LinearGradient colors={GRADIENTS.secondary} style={styles.receiptHeader}>
            <Icon name="ticket-confirmation" size={28} color={COLORS.white} style={{ marginBottom: 8 }} />
            <Text style={styles.receiptEmpresaNombre}>{empresa?.nombre || 'Mi Negocio'}</Text>
            {empresa?.direccion ? (
              <Text style={styles.receiptEmpresaDir}>{empresa.direccion}</Text>
            ) : null}
          </LinearGradient>

          <View style={styles.receiptBody}>
            {/* Meta */}
            <View style={styles.receiptMeta}>
              <View style={styles.metaItem}>
                <Text style={styles.metaLabel}>TICKET</Text>
                <Text style={styles.metaValue}>{ticket.numero}</Text>
              </View>
              <View style={styles.metaDivider} />
              <View style={styles.metaItem}>
                <Text style={styles.metaLabel}>FECHA</Text>
                <Text style={styles.metaValue}>{ticket.fecha}</Text>
              </View>
            </View>

            {/* Divisor punteado */}
            <View style={styles.dashedDivider}>
              {[...Array(20)].map((_, i) => <View key={i} style={styles.dash} />)}
            </View>

            <Text style={styles.productsTitle}>PRODUCTOS</Text>
            {ticket.items.map((item, idx) => (
              <View key={idx} style={styles.itemRow}>
                <Text style={styles.itemQtyName}>
                  <Text style={styles.itemQty}>{item.cantidad}x </Text>
                  {item.nombre}
                </Text>
                <Text style={styles.itemTotal}>${item.subtotal.toFixed(2)}</Text>
              </View>
            ))}

            <View style={styles.dashedDivider}>
              {[...Array(20)].map((_, i) => <View key={i} style={styles.dash} />)}
            </View>

            <View style={styles.totalRow}>
              <Text style={styles.totalLabel}>TOTAL</Text>
              <Text style={styles.totalValue}>${ticket.total.toFixed(2)}</Text>
            </View>

            <Text style={styles.thankYou}>¡Gracias por su compra!</Text>
          </View>
        </View>

        {/* ── Acciones de envío ── */}
        <Text style={styles.accionesTitle}>Enviar ticket</Text>
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
            colors={GRADIENTS.secondary}
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

  receiptCard: {
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radiusLg,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: COLORS.border,
    marginBottom: 20,
  },
  receiptHeader: {
    padding: SIZES.paddingLg,
    alignItems: 'center',
  },
  receiptEmpresaNombre: {
    color: COLORS.white,
    fontSize: SIZES.xl,
    fontWeight: '800',
    letterSpacing: -0.3,
  },
  receiptEmpresaDir: {
    color: 'rgba(255,255,255,0.75)',
    fontSize: SIZES.sm,
    marginTop: 4,
  },
  receiptBody: { padding: SIZES.paddingLg },
  receiptMeta: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    marginBottom: 16,
  },
  metaItem: { alignItems: 'center' },
  metaLabel: {
    color: COLORS.textMuted,
    fontSize: SIZES.xs,
    fontWeight: '700',
    letterSpacing: 1,
    marginBottom: 4,
  },
  metaValue: { color: COLORS.textPrimary, fontSize: SIZES.base, fontWeight: '700' },
  metaDivider: { width: 1, height: 32, backgroundColor: COLORS.border },
  dashedDivider: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 14,
  },
  dash: {
    width: 6, height: 1.5,
    backgroundColor: COLORS.borderLight,
    borderRadius: 1,
  },
  productsTitle: {
    color: COLORS.textMuted,
    fontSize: SIZES.xs,
    fontWeight: '700',
    letterSpacing: 1.2,
    marginBottom: 12,
  },
  itemRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  itemQtyName: {
    color: COLORS.textSecondary,
    fontSize: SIZES.sm,
    flex: 1,
    paddingRight: 8,
  },
  itemQty: { color: COLORS.secondary, fontWeight: '700' },
  itemTotal: { color: COLORS.textPrimary, fontSize: SIZES.sm, fontWeight: '700' },
  totalRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  totalLabel: { color: COLORS.textSecondary, fontSize: SIZES.base, fontWeight: '700' },
  totalValue: { color: COLORS.secondary, fontSize: SIZES.xxl, fontWeight: '800' },
  thankYou: {
    textAlign: 'center',
    marginTop: 20,
    color: COLORS.textMuted,
    fontSize: SIZES.sm,
    fontStyle: 'italic',
  },

  // Acciones
  accionesTitle: {
    color: COLORS.textSecondary,
    fontSize: SIZES.xs,
    fontWeight: '700',
    letterSpacing: 1.2,
    textTransform: 'uppercase',
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
