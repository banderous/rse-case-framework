package uk.gov.hmcts.unspec.xui;

import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccf.ColumnMapper;
import uk.gov.hmcts.ccf.WorkbasketInputBuilder;
import uk.gov.hmcts.ccf.XUIQuery;
import uk.gov.hmcts.ccf.XUISearchHandler;

import java.util.List;

import static org.jooq.generated.Tables.CASES_WITH_STATES;
import static org.jooq.impl.DSL.asterisk;
import static org.jooq.impl.DSL.count;


@Component
public class SearchHandler extends XUISearchHandler<CaseSearchResult> {

    @Autowired
    private DefaultDSLContext jooq;

    @Override
    public void configureWorkbasketInputs(WorkbasketInputBuilder builder) {
        builder.textInput("caseId", "Case ID");
    }

    @Override
    public void configureColumns(ColumnMapper<CaseSearchResult> mapper) {
        mapper.column(CaseSearchResult::getCaseId)
            .column(CaseSearchResult::getState)
            .column(CaseSearchResult::getParentCaseId);
    }

    public SearchResults search(XUIQuery query) {

        String id = query.getParams().get("caseId");
        Condition condition = DSL.trueCondition();
        if (id != null && id.length() > 0) {
            id = id.replaceAll("-", "");
            condition = condition.and(CASES_WITH_STATES.CASE_ID.equal(Long.valueOf(id)));
        }

        List<CaseSearchResult> results = jooq
            .select(asterisk(), count().over().as("rowCount"))
            .from(CASES_WITH_STATES)
            .where(condition)
            .orderBy(CASES_WITH_STATES.CASE_ID.asc())
            .offset(query.getFrom())
            .limit(query.getPageSize())
            .fetchInto(CaseSearchResult.class);

        long rowCount = results.isEmpty() ? 0 : results.get(0).getRowCount();
        return new SearchResults(rowCount, results);
    }
}
