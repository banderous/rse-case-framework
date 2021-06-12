{% for party in parties %}
    <h2>{{ party.data.name }}</h2>
    <dl class="govuk-summary-list">
        <div class="govuk-summary-list__row">
            <dt class="govuk-summary-list__key">
                Contact information
            </dt>
            <dd class="govuk-summary-list__value">
                {{ party.data.name }}<br>
                {{ party.data.address.address1 }}<br>London<br>SE23 6FH
            </dd>
            <dd class="govuk-summary-list__actions">
                <a class="govuk-link" href="/cases/case-details/{{caseId}}/trigger/cases_ChangePartyAddress_{{party.partyId}}">
                    Change<span class="govuk-visually-hidden"> contact information</span>
                </a>
            </dd>
        </div>
        <div class="govuk-summary-list__row">
            <dt class="govuk-summary-list__key">
                Contact details
            </dt>
            <dd class="govuk-summary-list__value">
                <a href="mailto:{{ party.data.name | lower | replace({ ' ': '_'}) }}@example.com" class="govuk-body">{{ party.data.name | lower | replace({ ' ': '_'}) }}@example.com</a>
            </dd>
            <dd class="govuk-summary-list__actions">
                <a class="govuk-link" href="#">
                    Change<span class="govuk-visually-hidden"> contact details</span>
                </a>
            </dd>
        </div>
    </dl>
{% endfor %}
