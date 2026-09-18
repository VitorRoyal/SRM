import styled from "styled-components";
import { NavLink } from "react-router-dom";
import { colors } from "../../styles/colors";

export const Header = styled.header`
  background: ${colors.navy};
  color: ${colors.white};
`;

export const HeaderContent = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  min-height: 64px;
  display: flex;
  align-items: center;
  gap: 32px;
  flex-wrap: wrap;
`;

export const Brand = styled.span`
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.02em;
`;

export const Navigation = styled.nav`
  display: flex;
  gap: 4px;
  flex: 1;
  flex-wrap: wrap;
`;

export const NavigationLink = styled(NavLink)`
  padding: 8px 12px;
  border-radius: 8px;
  color: ${colors.navyText};
  text-decoration: none;
  font-weight: 500;

  &:hover {
    color: ${colors.white};
  }

  &.active {
    background: ${colors.primary};
    color: ${colors.white};
  }
`;

export const LanguageField = styled.label`
  display: flex;
  align-items: center;
  gap: 8px;
  color: ${colors.navyText};
  font-size: 12px;
`;

export const LanguageSelect = styled.select`
  height: 32px;
  padding: 0 8px;
  border-radius: 6px;
  border: 1px solid ${colors.primary};
  background: ${colors.navy};
  color: ${colors.white};
`;
