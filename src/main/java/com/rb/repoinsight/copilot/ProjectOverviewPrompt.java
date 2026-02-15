package com.rb.repoinsight.copilot;

import com.rb.repoinsight.constants.PromptsConfig;
import com.rb.repoinsight.model.RepoContext;

public class ProjectOverviewPrompt {

    public static String buildPrompt(RepoContext context) {
        return PromptsConfig.buildProjectOverviewPrompt(context);
    }
}
