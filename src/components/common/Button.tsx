import React from 'react';
import { TouchableOpacity, Text, StyleSheet, ActivityIndicator, View } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { COLORS, SIZES, SHADOWS, GRADIENTS } from '../../theme';

interface ButtonProps {
  label: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'success';
  loading?: boolean;
  disabled?: boolean;
  iconName?: string;
  style?: object;
}

export const Button = ({
  label, onPress, variant = 'primary', loading, disabled, iconName, style,
}: ButtonProps) => {
  const gradients: Record<string, string[]> = {
    primary: GRADIENTS.primary,
    secondary: GRADIENTS.secondary,
    danger: [COLORS.error, '#CC2233'],
    ghost: [COLORS.transparent, COLORS.transparent],
    success: GRADIENTS.success,
  };

  const isGhost = variant === 'ghost';
  const textColor = isGhost ? COLORS.primary : COLORS.white;

  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={disabled || loading}
      style={[disabled && styles.disabled, style]}
      activeOpacity={0.75}
    >
      <LinearGradient
        colors={gradients[variant]}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 0 }}
        style={[styles.btn, isGhost && styles.ghost, !isGhost && SHADOWS.md]}
      >
        {loading ? (
          <ActivityIndicator color={textColor} size="small" />
        ) : (
          <View style={styles.row}>
            {iconName ? <Icon name={iconName} size={18} color={textColor} style={styles.icon} /> : null}
            <Text style={[styles.label, { color: textColor }]}>{label}</Text>
          </View>
        )}
      </LinearGradient>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  btn: {
    borderRadius: SIZES.radiusLg,
    paddingVertical: 15,
    paddingHorizontal: SIZES.paddingLg,
    alignItems: 'center',
    justifyContent: 'center',
  },
  ghost: {
    borderWidth: 1.5,
    borderColor: COLORS.primary,
    backgroundColor: COLORS.transparent,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  icon: { marginRight: 8 },
  label: { fontSize: SIZES.base, fontWeight: '700', letterSpacing: 0.3 },
  disabled: { opacity: 0.45 },
});
