import styled from "styled-components";

export const FiltersGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr)) auto;
  gap: 16px;
  align-items: end;

  @media (max-width: 960px) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  @media (max-width: 560px) {
    grid-template-columns: minmax(0, 1fr);
  }
`;
