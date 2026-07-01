import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet, TextInputProps } from 'react-native';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import { COLORS, SIZES } from '../../theme';

interface InputProps extends TextInputProps {
  label?: string;
  error?: string;
  iconName?: string;
}

export const Input = ({ label, error, iconName, style, ...props }: InputProps) => {
  const [focused, setFocused] = useState(false);

  const borderColor = error
    ? COLORS.error
    : focused
    ? COLORS.primary
    : COLORS.border;

  return (
    <View style={styles.wrapper}>
      {label ? <Text style={styles.label}>{label}</Text> : null}
      <View style={[styles.inputRow, { borderColor }]}>
        {iconName ? (
          <Icon
            name={iconName}
            size={18}
            color={focused ? COLORS.primary : COLORS.textMuted}
            style={styles.icon}
          />
        ) : null}
        <TextInput
          style={[styles.input, style]}
          placeholderTextColor={COLORS.textMuted}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          {...props}
        />
      </View>
      {error ? <Text style={styles.error}>{error}</Text> : null}
    </View>
  );
};

const styles = StyleSheet.create({
  wrapper: { marginBottom: 16 },
  label: {
    color: COLORS.textSecondary,
    fontSize: SIZES.sm,
    marginBottom: 7,
    fontWeight: '600',
    letterSpacing: 0.2,
  },
  inputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.backgroundInput,
    borderRadius: SIZES.radiusSm,
    borderWidth: 1.5,
    paddingHorizontal: SIZES.padding,
  },
  icon: { marginRight: 10 },
  input: {
    flex: 1,
    color: COLORS.textPrimary,
    paddingVertical: 13,
    fontSize: SIZES.base,
  },
  error: { color: COLORS.error, fontSize: SIZES.xs, marginTop: 5, marginLeft: 2 },
});
