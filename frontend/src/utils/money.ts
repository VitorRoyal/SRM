const MAX_INTEGER_DIGITS = 13;

const PLAIN_INTEGER_PATTERN = /^\d+$/;
const DOT_GROUPED_INTEGER_PATTERN = /^\d{1,3}(\.\d{3})+$/;
const COMMA_GROUPED_INTEGER_PATTERN = /^\d{1,3}(,\d{3})+$/;
const FRACTION_PATTERN = /^\d{1,2}$/;
const RATE_PATTERN = /^\d{1,6}(\.\d{1,8})?$/;

const isValidIntegerPart = (integerPart: string): boolean =>
  PLAIN_INTEGER_PATTERN.test(integerPart) ||
  DOT_GROUPED_INTEGER_PATTERN.test(integerPart) ||
  COMMA_GROUPED_INTEGER_PATTERN.test(integerPart);

export const normalizeDecimalInput = (rawValue: string): string | null => {
  const compactValue = rawValue.replace(/\s/g, "");
  if (compactValue.length === 0) {
    return null;
  }

  let integerPart = compactValue;
  let fractionPart = "";
  const lastSeparatorIndex = Math.max(compactValue.lastIndexOf(","), compactValue.lastIndexOf("."));
  if (lastSeparatorIndex >= 0) {
    const candidateFraction = compactValue.slice(lastSeparatorIndex + 1);
    if (FRACTION_PATTERN.test(candidateFraction)) {
      integerPart = compactValue.slice(0, lastSeparatorIndex);
      fractionPart = candidateFraction;
    }
  }

  if (!isValidIntegerPart(integerPart)) {
    return null;
  }

  const integerDigits = integerPart.replace(/[.,]/g, "");
  if (integerDigits.replace(/^0+(?=\d)/, "").length > MAX_INTEGER_DIGITS) {
    return null;
  }

  if (fractionPart.length === 0) {
    return integerDigits;
  }
  return `${integerDigits}.${fractionPart}`;
};

export const normalizeRateInput = (rawValue: string): string | null => {
  const normalizedValue = rawValue.trim().replace(",", ".");
  if (!RATE_PATTERN.test(normalizedValue)) {
    return null;
  }
  return normalizedValue;
};

export const isPositiveDecimal = (normalizedValue: string | null): normalizedValue is string =>
  normalizedValue !== null && Number(normalizedValue) > 0;
