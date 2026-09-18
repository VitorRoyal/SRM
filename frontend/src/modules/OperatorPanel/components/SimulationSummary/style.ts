import styled from "styled-components";
import { colors } from "../../../../styles/colors";

export const SummaryHeader = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
`;

export const HighlightBox = styled.div`
  padding: 20px;
  border-radius: 10px;
  background: ${colors.primarySoft};
  margin-bottom: 16px;
`;

export const HighlightLabel = styled.span`
  display: block;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: ${colors.primary};
`;

export const HighlightValue = styled.span`
  display: block;
  margin-top: 4px;
  font-size: 30px;
  font-weight: 700;
  color: ${colors.navy};
  font-variant-numeric: tabular-nums;
`;

export const DetailList = styled.dl`
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 10px 16px;
  margin: 0 0 20px;
`;

export const DetailTerm = styled.dt`
  color: ${colors.textSecondary};
`;

export const DetailValue = styled.dd`
  margin: 0;
  text-align: right;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
`;

export const SettleButtonWrapper = styled.div`
  display: flex;

  & > button {
    flex: 1;
  }
`;
