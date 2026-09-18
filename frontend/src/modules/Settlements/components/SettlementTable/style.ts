import styled from "styled-components";
import { colors } from "../../../../styles/colors";

export const TableWrapper = styled.div`
  overflow-x: auto;
  border: 1px solid ${colors.border};
  border-radius: 12px;
  background: ${colors.surface};
`;

export const Table = styled.table`
  width: 100%;
  border-collapse: collapse;
  min-width: 960px;
`;

export const HeaderCell = styled.th<{ $numeric?: boolean }>`
  padding: 12px 16px;
  text-align: ${({ $numeric }) => ($numeric ? "right" : "left")};
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: ${colors.textSecondary};
  background: ${colors.surfaceMuted};
  border-bottom: 1px solid ${colors.border};
  white-space: nowrap;
`;

export const BodyRow = styled.tr`
  &:not(:last-child) td {
    border-bottom: 1px solid ${colors.border};
  }

  &:hover td {
    background: ${colors.surfaceMuted};
  }
`;

export const BodyCell = styled.td<{ $numeric?: boolean; $emphasis?: boolean }>`
  padding: 12px 16px;
  text-align: ${({ $numeric }) => ($numeric ? "right" : "left")};
  font-variant-numeric: tabular-nums;
  font-weight: ${({ $emphasis }) => ($emphasis ? 600 : 400)};
  white-space: nowrap;
`;

export const EmptyCell = styled.td`
  padding: 32px 16px;
  text-align: center;
  color: ${colors.textMuted};
`;

export const SecondaryLine = styled.span`
  display: block;
  font-size: 12px;
  color: ${colors.textMuted};
`;
