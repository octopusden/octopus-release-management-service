# Hotfix Issue Assignment

As part of the hotfix version registration support in Releng, it will now **automatically assign resolved Jira issues** to a hotfix version **based on the issues referenced in VCS commit changes**.

## Rules
- The assignment rules are the same as for regular releases: **only resolved issues are eligible for assignment before the RC is performed**. 
- An issue **will not be assigned** to a hotfix version **if it already has a hotfix version** from the **same regular release** or from its **actual parent release**.

Please note that in the examples provided ([hotfix-issue-assignment/](hotfix-issue-assignment/)), all issues referenced in the commits were already resolved before the RCs.