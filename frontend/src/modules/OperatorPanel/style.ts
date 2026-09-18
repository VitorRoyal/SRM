import styled from "styled-components";

export const PanelGrid = styled.div`
  display: grid;
  grid-template-columns: minmax(0, 3fr) minmax(0, 2fr);
  gap: 24px;
  align-items: start;

  @media (max-width: 960px) {
    grid-template-columns: minmax(0, 1fr);
  }
`;

export const SideColumn = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
`;
