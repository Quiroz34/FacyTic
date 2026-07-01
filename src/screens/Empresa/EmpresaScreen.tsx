import React, { useState, useEffect } from 'react';
import {
  View, Text, ScrollView, TouchableOpacity,
  StyleSheet, TextInput, Alert, Image, ActionSheetIOS, Platform,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import {
  launchImageLibrary,
  launchCamera,
  type ImagePickerResponse,
} from 'react-native-image-picker';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';
import { useAppStore } from '../../store/useAppStore';

// ─── Campo de formulario ──────────────────────────────────────────────────────

interface CampoProps {
  label: string;
  value: string;
  onChangeText: (v: string) => void;
  placeholder?: string;
  keyboardType?: any;
  multiline?: boolean;
  iconName: string;
}

const Campo = ({ label, value, onChangeText, placeholder, keyboardType, multiline, iconName }: CampoProps) => {
  const [focused, setFocused] = useState(false);
  return (
    <View style={styles.campo}>
      <Text style={styles.campoLabel}>{label}</Text>
      <View style={[styles.campoRow, focused && styles.campoRowFocused]}>
        <Icon name={iconName} size={17} color={focused ? COLORS.primary : COLORS.textMuted} style={styles.campoIcon} />
        <TextInput
          style={[styles.campoInput, multiline && styles.multiline]}
          value={value}
          onChangeText={onChangeText}
          placeholder={placeholder ?? label}
          placeholderTextColor={COLORS.textMuted}
          keyboardType={keyboardType ?? 'default'}
          multiline={multiline}
          numberOfLines={multiline ? 3 : 1}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
        />
      </View>
    </View>
  );
};

// ─── Encabezado de sección ────────────────────────────────────────────────────

const SectionHeader = ({ iconName, title }: { iconName: string; title: string }) => (
  <View style={styles.sectionHeader}>
    <View style={styles.sectionIconBox}>
      <Icon name={iconName} size={15} color={COLORS.primary} />
    </View>
    <Text style={styles.sectionTitle}>{title}</Text>
  </View>
);

// ─── Opciones del image picker ─────────────────────────────────────────────────

const IMAGE_OPTIONS = {
  mediaType: 'photo' as const,
  quality: 0.8 as const,
  maxWidth: 800,
  maxHeight: 800,
  includeBase64: false,
};

// ─── Pantalla principal ───────────────────────────────────────────────────────

export const EmpresaScreen = () => {
  const { empresa, saveEmpresa } = useAppStore();

  const [nombre, setNombre]       = useState('');
  const [slogan, setSlogan]       = useState('');
  const [logo, setLogo]           = useState<string | undefined>(undefined);
  const [direccion, setDireccion] = useState('');
  const [ciudad, setCiudad]       = useState('');
  const [telefono, setTelefono]   = useState('');
  const [celular, setCelular]     = useState('');
  const [correo, setCorreo]       = useState('');
  const [sitioWeb, setSitioWeb]   = useState('');
  const [ruc, setRuc]             = useState('');

  useEffect(() => {
    if (empresa) {
      setNombre(empresa.nombre ?? '');
      setSlogan(empresa.slogan ?? '');
      setLogo(empresa.logo ?? undefined);
      setDireccion(empresa.direccion ?? '');
      setCiudad(empresa.ciudad ?? '');
      setTelefono(empresa.telefono ?? '');
      setCelular(empresa.celular ?? '');
      setCorreo(empresa.correo ?? '');
      setSitioWeb(empresa.sitioWeb ?? '');
      setRuc(empresa.ruc ?? '');
    }
  }, [empresa]);

  // ── Selección de imagen ──────────────────────────────────────────────────

  const handleImageResult = (response: ImagePickerResponse) => {
    if (response.didCancel || response.errorCode) return;
    const uri = response.assets?.[0]?.uri;
    if (uri) setLogo(uri);
  };

  const pickFromGallery = () => {
    launchImageLibrary(IMAGE_OPTIONS, handleImageResult);
  };

  const pickFromCamera = () => {
    launchCamera(IMAGE_OPTIONS, handleImageResult);
  };

  const removeLogo = () => {
    Alert.alert('Eliminar logo', '¿Deseas quitar el logo de tu empresa?', [
      { text: 'Cancelar', style: 'cancel' },
      { text: 'Eliminar', style: 'destructive', onPress: () => setLogo(undefined) },
    ]);
  };

  const handleLogoPress = () => {
    if (Platform.OS === 'ios') {
      ActionSheetIOS.showActionSheetWithOptions(
        {
          options: logo
            ? ['Cancelar', 'Galería', 'Cámara', 'Eliminar logo']
            : ['Cancelar', 'Galería', 'Cámara'],
          cancelButtonIndex: 0,
          destructiveButtonIndex: logo ? 3 : undefined,
        },
        (buttonIndex) => {
          if (buttonIndex === 1) pickFromGallery();
          if (buttonIndex === 2) pickFromCamera();
          if (buttonIndex === 3 && logo) removeLogo();
        },
      );
    } else {
      const options = ['Seleccionar de galería', 'Tomar foto'];
      if (logo) options.push('Eliminar logo');
      options.push('Cancelar');

      Alert.alert('Logo de empresa', '¿Cómo deseas agregar el logo?', [
        { text: 'Seleccionar de galería', onPress: pickFromGallery },
        { text: 'Tomar foto', onPress: pickFromCamera },
        ...(logo ? [{ text: 'Eliminar logo', style: 'destructive' as const, onPress: removeLogo }] : []),
        { text: 'Cancelar', style: 'cancel' as const },
      ]);
    }
  };

  // ── Guardar ──────────────────────────────────────────────────────────────

  const guardar = () => {
    if (!nombre.trim()) {
      Alert.alert('Campo requerido', 'El nombre de la empresa es obligatorio');
      return;
    }
    try {
      saveEmpresa({ nombre, slogan, logo, direccion, ciudad, telefono, celular, correo, sitioWeb, ruc });
      Alert.alert('Guardado', 'Los datos de tu empresa fueron actualizados.');
    } catch (e: any) {
      console.error('[EmpresaScreen] guardar:', e);
      Alert.alert('Error al guardar', e?.message ?? 'No se pudieron guardar los datos. Intenta de nuevo.');
    }
  };

  // ── Render ───────────────────────────────────────────────────────────────

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
    >
      {/* ── Avatar / Logo ── */}
      <View style={styles.avatarSection}>
        <TouchableOpacity onPress={handleLogoPress} activeOpacity={0.8} style={styles.avatarWrapper}>
          {logo ? (
            <>
              <Image source={{ uri: logo }} style={styles.logoImage} resizeMode="cover" />
              {/* Badge de editar encima de la imagen */}
              <View style={styles.editBadge}>
                <Icon name="pencil" size={14} color={COLORS.white} />
              </View>
            </>
          ) : (
            <LinearGradient colors={GRADIENTS.primary} style={[styles.avatarContainer, SHADOWS.lg]}>
              {nombre ? (
                <Text style={styles.avatarText}>{nombre[0].toUpperCase()}</Text>
              ) : (
                <Icon name="domain" size={40} color={COLORS.white} />
              )}
              {/* Badge de agregar */}
              <View style={styles.addBadge}>
                <Icon name="camera-plus-outline" size={14} color={COLORS.white} />
              </View>
            </LinearGradient>
          )}
        </TouchableOpacity>

        <TouchableOpacity onPress={handleLogoPress} style={styles.logoHint} activeOpacity={0.7}>
          <Icon name="camera-outline" size={14} color={COLORS.primary} />
          <Text style={styles.logoHintText}>
            {logo ? 'Cambiar logo' : 'Agregar logo'}
          </Text>
        </TouchableOpacity>

        {nombre ? <Text style={styles.avatarName}>{nombre}</Text> : null}
        {slogan ? <Text style={styles.avatarSlogan}>{slogan}</Text> : null}
      </View>

      {/* ── Información básica ── */}
      <View style={styles.card}>
        <SectionHeader iconName="information-outline" title="Información básica" />
        <Campo label="Nombre de la empresa *" value={nombre} onChangeText={setNombre} iconName="office-building-outline" />
        <Campo label="Slogan" value={slogan} onChangeText={setSlogan} placeholder="Tu eslogan aquí" iconName="tag-text-outline" />
        <Campo label="RUC / NIT / ID Fiscal" value={ruc} onChangeText={setRuc} iconName="card-account-details-outline" />
      </View>

      {/* ── Ubicación ── */}
      <View style={styles.card}>
        <SectionHeader iconName="map-marker-outline" title="Ubicación" />
        <Campo label="Dirección" value={direccion} onChangeText={setDireccion} iconName="map-marker-outline" />
        <Campo label="Ciudad" value={ciudad} onChangeText={setCiudad} iconName="city-variant-outline" />
      </View>

      {/* ── Contacto ── */}
      <View style={styles.card}>
        <SectionHeader iconName="contacts-outline" title="Contacto" />
        <Campo label="Teléfono" value={telefono} onChangeText={setTelefono} keyboardType="phone-pad" iconName="phone-outline" />
        <Campo label="Celular / WhatsApp" value={celular} onChangeText={setCelular} keyboardType="phone-pad" iconName="whatsapp" />
        <Campo label="Correo electrónico" value={correo} onChangeText={setCorreo} keyboardType="email-address" iconName="email-outline" />
        <Campo label="Sitio web" value={sitioWeb} onChangeText={setSitioWeb} iconName="web" />
      </View>

      {/* ── Botón guardar ── */}
      <TouchableOpacity onPress={guardar} activeOpacity={0.8} style={SHADOWS.lg}>
        <LinearGradient
          colors={GRADIENTS.primary}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 0 }}
          style={styles.btnGuardar}
        >
          <Icon name="content-save-outline" size={20} color={COLORS.white} style={{ marginRight: 8 }} />
          <Text style={styles.btnGuardarText}>Guardar cambios</Text>
        </LinearGradient>
      </TouchableOpacity>
    </ScrollView>
  );
};

const AVATAR_SIZE = 100;

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  content: { padding: SIZES.padding, paddingBottom: 60 },

  // ── Avatar / Logo ──
  avatarSection: { alignItems: 'center', paddingVertical: SIZES.paddingLg },
  avatarWrapper: {
    width: AVATAR_SIZE,
    height: AVATAR_SIZE,
    borderRadius: AVATAR_SIZE / 2,
    marginBottom: 10,
    position: 'relative',
  },
  logoImage: {
    width: AVATAR_SIZE,
    height: AVATAR_SIZE,
    borderRadius: AVATAR_SIZE / 2,
    borderWidth: 3,
    borderColor: COLORS.primary,
  },
  avatarContainer: {
    width: AVATAR_SIZE,
    height: AVATAR_SIZE,
    borderRadius: AVATAR_SIZE / 2,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: { color: COLORS.white, fontSize: 42, fontWeight: '800' },
  editBadge: {
    position: 'absolute',
    bottom: 2,
    right: 2,
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: COLORS.primary,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: COLORS.background,
  },
  addBadge: {
    position: 'absolute',
    bottom: 2,
    right: 2,
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: COLORS.primaryDark,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: COLORS.background,
  },
  logoHint: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: SIZES.radiusFull,
    backgroundColor: COLORS.primary + '18',
    borderWidth: 1,
    borderColor: COLORS.primary + '44',
    marginBottom: 12,
  },
  logoHintText: { color: COLORS.primary, fontSize: SIZES.sm, fontWeight: '600' },
  avatarName: {
    color: COLORS.textPrimary,
    fontSize: SIZES.xl,
    fontWeight: '800',
    letterSpacing: -0.3,
  },
  avatarSlogan: { color: COLORS.textSecondary, fontSize: SIZES.sm, marginTop: 4 },

  // ── Cards / campos ──
  card: {
    backgroundColor: COLORS.backgroundCard,
    borderRadius: SIZES.radiusLg,
    padding: SIZES.padding,
    marginBottom: 14,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: SIZES.padding,
    paddingBottom: SIZES.paddingSm,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
  },
  sectionIconBox: {
    width: 28,
    height: 28,
    borderRadius: 8,
    backgroundColor: COLORS.primary + '22',
    alignItems: 'center',
    justifyContent: 'center',
  },
  sectionTitle: {
    color: COLORS.textPrimary,
    fontSize: SIZES.sm,
    fontWeight: '700',
    letterSpacing: 0.3,
    textTransform: 'uppercase',
  },
  campo: { marginBottom: 14 },
  campoLabel: {
    color: COLORS.textSecondary,
    fontSize: SIZES.xs,
    fontWeight: '600',
    marginBottom: 7,
    letterSpacing: 0.2,
  },
  campoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.backgroundInput,
    borderRadius: SIZES.radiusSm,
    borderWidth: 1.5,
    borderColor: COLORS.border,
    paddingHorizontal: 12,
  },
  campoRowFocused: { borderColor: COLORS.primary },
  campoIcon: { marginRight: 10 },
  campoInput: {
    flex: 1,
    color: COLORS.textPrimary,
    paddingVertical: 12,
    fontSize: SIZES.base,
  },
  multiline: { textAlignVertical: 'top', minHeight: 80 },

  // ── Botón guardar ──
  btnGuardar: {
    borderRadius: SIZES.radiusLg,
    paddingVertical: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: SIZES.marginSm,
  },
  btnGuardarText: { color: COLORS.white, fontSize: SIZES.lg, fontWeight: '700', letterSpacing: 0.3 },
});
