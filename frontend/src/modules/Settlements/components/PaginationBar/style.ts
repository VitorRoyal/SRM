import styled from "styled-components";
import { colors } from "../../../../styles/colors";

export const PaginationContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
`;

export const PaginationGroup = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const PaginationInfo = styled.span`
  color: ${colors.textSecondary};
`;

export const PageSizeSelect = styled.select`
  height: 40px;
  padding: 0 8px;
  border-radius: 8px;
  border: 1px solid ${colors.borderStrong};
  background: ${colors.surface};
`;
