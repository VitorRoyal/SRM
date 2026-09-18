import React from "react";
import { useTranslation } from "react-i18next";
import { SecondaryButton } from "../../../../styles/GlobalStyles";
import { PageSizeSelect, PaginationContainer, PaginationGroup, PaginationInfo } from "./style";

const PAGE_SIZE_OPTIONS = [10, 20, 50];

interface PaginationBarProps {
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  isLoading: boolean;
  onPageChange: (page: number) => void;
  onSizeChange: (size: number) => void;
}

export const PaginationBar: React.FC<PaginationBarProps> = ({
  page,
  size,
  totalPages,
  totalElements,
  isLoading,
  onPageChange,
  onSizeChange,
}) => {
  const { t } = useTranslation();
  const displayedTotalPages = Math.max(totalPages, 1);

  return (
    <PaginationContainer>
      <PaginationGroup>
        <PaginationInfo>{t("settlements.pagination.totalInfo", { count: totalElements })}</PaginationInfo>
        <PaginationInfo>
          {t("settlements.pagination.pageInfo", { page: page + 1, totalPages: displayedTotalPages })}
        </PaginationInfo>
      </PaginationGroup>
      <PaginationGroup>
        <PaginationInfo>{t("settlements.pagination.pageSize")}</PaginationInfo>
        <PageSizeSelect value={size} onChange={(event) => onSizeChange(Number(event.target.value))}>
          {PAGE_SIZE_OPTIONS.map((pageSizeOption) => (
            <option key={pageSizeOption} value={pageSizeOption}>
              {pageSizeOption}
            </option>
          ))}
        </PageSizeSelect>
        <SecondaryButton type="button" disabled={isLoading || page === 0} onClick={() => onPageChange(page - 1)}>
          {t("settlements.pagination.previous")}
        </SecondaryButton>
        <SecondaryButton
          type="button"
          disabled={isLoading || page + 1 >= displayedTotalPages}
          onClick={() => onPageChange(page + 1)}
        >
          {t("settlements.pagination.next")}
        </SecondaryButton>
      </PaginationGroup>
    </PaginationContainer>
  );
};
