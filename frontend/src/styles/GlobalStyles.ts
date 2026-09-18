import styled, { createGlobalStyle, css } from "styled-components";
import { colors } from "./colors";

export const GlobalReset = createGlobalStyle`
  *, *::before, *::after {
    box-sizing: border-box;
  }

  body {
    margin: 0;
    background: ${colors.background};
    color: ${colors.textPrimary};
    font-family: "Inter", "Segoe UI", system-ui, -apple-system, sans-serif;
    font-size: 14px;
    line-height: 1.5;
    -webkit-font-smoothing: antialiased;
  }

  button, input, select {
    font: inherit;
  }
`;

export const PageContainer = styled.main`
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px 64px;
`;

export const PageHeader = styled.header`
  margin-bottom: 24px;
`;

export const Title = styled.h1`
  margin: 0;
  font-size: 24px;
  font-weight: 700;
`;

export const Subtitle = styled.p`
  margin: 4px 0 0;
  color: ${colors.textSecondary};
`;

export const SectionTitle = styled.h2`
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
`;

export const Card = styled.section`
  background: ${colors.surface};
  border: 1px solid ${colors.border};
  border-radius: 12px;
  padding: 24px;
`;

export const Column = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

export const Row = styled.div<{ $columns?: number }>`
  display: grid;
  grid-template-columns: repeat(${({ $columns }) => $columns ?? 2}, minmax(0, 1fr));
  gap: 16px;

  @media (max-width: 720px) {
    grid-template-columns: minmax(0, 1fr);
  }
`;

export const Field = styled.label`
  display: flex;
  flex-direction: column;
  gap: 6px;
`;

export const Label = styled.span`
  font-size: 12px;
  font-weight: 600;
  color: ${colors.textSecondary};
  text-transform: uppercase;
  letter-spacing: 0.04em;
`;

const controlStyles = css<{ $invalid?: boolean }>`
  height: 40px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid ${({ $invalid }) => ($invalid ? colors.danger : colors.borderStrong)};
  background: ${colors.surface};
  color: ${colors.textPrimary};
  outline: none;

  &:focus {
    border-color: ${colors.primary};
    box-shadow: 0 0 0 3px ${colors.focusRing};
  }

  &:disabled {
    background: ${colors.surfaceMuted};
    color: ${colors.textMuted};
  }
`;

export const Input = styled.input<{ $invalid?: boolean }>`
  ${controlStyles}
`;

export const Select = styled.select<{ $invalid?: boolean }>`
  ${controlStyles}
`;

export const FieldError = styled.span`
  font-size: 12px;
  color: ${colors.danger};
`;

export const PrimaryButton = styled.button`
  height: 44px;
  padding: 0 20px;
  border: none;
  border-radius: 8px;
  background: ${colors.primary};
  color: ${colors.white};
  font-weight: 600;
  cursor: pointer;

  &:hover:not(:disabled) {
    background: ${colors.primaryHover};
  }

  &:disabled {
    background: ${colors.borderStrong};
    cursor: not-allowed;
  }
`;

export const SecondaryButton = styled.button`
  height: 40px;
  padding: 0 16px;
  border: 1px solid ${colors.borderStrong};
  border-radius: 8px;
  background: ${colors.surface};
  color: ${colors.textPrimary};
  font-weight: 500;
  cursor: pointer;

  &:hover:not(:disabled) {
    background: ${colors.surfaceMuted};
  }

  &:disabled {
    color: ${colors.textMuted};
    cursor: not-allowed;
  }
`;

export const MutedText = styled.p`
  margin: 0;
  color: ${colors.textMuted};
`;

export const ErrorText = styled.p`
  margin: 0;
  padding: 12px;
  border-radius: 8px;
  background: ${colors.dangerSoft};
  color: ${colors.danger};
`;

export const Badge = styled.span<{ $tone?: "primary" | "accent" }>`
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: ${({ $tone }) => ($tone === "accent" ? colors.accentSoft : colors.primarySoft)};
  color: ${({ $tone }) => ($tone === "accent" ? colors.accent : colors.primary)};
`;
