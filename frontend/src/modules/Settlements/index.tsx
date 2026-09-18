import React, { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "react-toastify";
import { ErrorText, PageContainer, PageHeader, Subtitle, Title } from "../../styles/GlobalStyles";
import { useAssignors } from "../../hooks/useAssignors";
import { extractApiErrorMessage } from "../../utils/apiError";
import type { PageResponse } from "../../types/Page";
import { StatementFiltersBar } from "./components/StatementFiltersBar";
import { SettlementTable } from "./components/SettlementTable";
import { PaginationBar } from "./components/PaginationBar";
import { getStatement } from "./service";
import type { SettlementRow, StatementFilters } from "./types";
import { StatementLayout } from "./style";

const STATEMENT_TOAST_ID = "settlements-statement";

const EMPTY_FILTERS: StatementFilters = {
  settledFrom: "",
  settledTo: "",
  assignorId: "",
  paymentCurrency: "",
};

const EMPTY_PAGE: PageResponse<SettlementRow> = {
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
};

export const Settlements: React.FC = () => {
  const { t } = useTranslation();
  const { assignors, isLoadingAssignors } = useAssignors("settlements.service.assignorsError");

  const [filters, setFilters] = useState<StatementFilters>(EMPTY_FILTERS);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [statementPage, setStatementPage] = useState<PageResponse<SettlementRow>>(EMPTY_PAGE);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const abortController = new AbortController();

    const loadStatement = async () => {
      setIsLoading(true);
      try {
        const result = await getStatement(filters, page, size, abortController.signal);
        setStatementPage(result);
        setErrorMessage(null);
        toast.dismiss(STATEMENT_TOAST_ID);
      } catch (error: unknown) {
        if (abortController.signal.aborted) {
          return;
        }
        const message = extractApiErrorMessage(error, "settlements.service.fetchError");
        setErrorMessage(message);
        toast.error(message, { toastId: STATEMENT_TOAST_ID });
      } finally {
        if (!abortController.signal.aborted) {
          setIsLoading(false);
        }
      }
    };

    loadStatement();
    return () => abortController.abort();
  }, [filters, page, size]);

  const handleFiltersChange = useCallback((nextFilters: StatementFilters) => {
    setFilters(nextFilters);
    setPage(0);
  }, []);

  const handleClearFilters = useCallback(() => {
    setFilters(EMPTY_FILTERS);
    setPage(0);
  }, []);

  const handleSizeChange = useCallback((nextSize: number) => {
    setSize(nextSize);
    setPage(0);
  }, []);

  return (
    <PageContainer>
      <PageHeader>
        <Title>{t("settlements.title")}</Title>
        <Subtitle>{t("settlements.subtitle")}</Subtitle>
      </PageHeader>
      <StatementLayout>
        <StatementFiltersBar
          filters={filters}
          assignors={assignors}
          isLoadingAssignors={isLoadingAssignors}
          onFiltersChange={handleFiltersChange}
          onClear={handleClearFilters}
        />
        {errorMessage && <ErrorText>{errorMessage}</ErrorText>}
        <SettlementTable settlements={statementPage.content} />
        <PaginationBar
          page={page}
          size={size}
          totalPages={statementPage.totalPages}
          totalElements={statementPage.totalElements}
          isLoading={isLoading}
          onPageChange={setPage}
          onSizeChange={handleSizeChange}
        />
      </StatementLayout>
    </PageContainer>
  );
};
