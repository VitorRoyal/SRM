import styled from "styled-components";
import { colors } from "../../styles/colors";

export const RatesGrid = styled.div`
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 24px;
  align-items: start;

  @media (max-width: 860px) {
    grid-template-columns: minmax(0, 1fr);
  }
`;

export const RateValue = styled.p`
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: ${colors.navy};
  font-variant-numeric: tabular-nums;
`;

export const RateMeta = styled.p`
  margin: 8px 0 0;
  color: ${colors.textSecondary};
`;

export const FormStack = styled.form`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

export const Hint = styled.p`
  margin: 0;
  font-size: 12px;
  color: ${colors.textMuted};
`;
