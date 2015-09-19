package com.univeris.cxp.scoringstrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public interface IScoringMethod {

	public Integer score(
			com.univeris.cxp.questionnaire.AnsweredQuestionnaire answeredQuesionnaire);

}
