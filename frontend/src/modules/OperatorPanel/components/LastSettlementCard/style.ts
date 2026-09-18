import styled from "styled-components";
import { Link } from "react-router-dom";
import { colors } from "../../../../styles/colors";

export const LastSettlementContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
`;

export const LastSettlementLine = styled.p`
  margin: 0;
  color: ${colors.textSecondary};
  font-variant-numeric: tabular-nums;
`;

export const StatementLink = styled(Link)`
  margin-top: 8px;
  color: ${colors.primary};
  font-weight: 600;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
`;
