/**
 * Send notification to Slack
 * 
 * @param status Build status (SUCCESS, FAILURE, UNSTABLE)
 * @param channel Slack channel to notify
 */
def call(String status, String channel = '#builds') {
    def color = 'good'
    def emoji = ':white_check_mark:'
    
    if (status == 'FAILURE') {
        color = 'danger'
        emoji = ':x:'
    } else if (status == 'UNSTABLE') {
        color = 'warning'
        emoji = ':warning:'
    }
    
    def message = """
        ${emoji} *${status}*: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'
        *Branch*: ${env.BRANCH_NAME ?: 'N/A'}
        *Duration*: ${currentBuild.durationString.replace(' and counting', '')}
        *URL*: ${env.BUILD_URL}
    """.stripIndent()
    
    try {
        slackSend(
            channel: channel,
            color: color,
            message: message,
            tokenCredentialId: 'slack-token'
        )
    } catch (Exception e) {
        echo "Failed to send Slack notification: ${e.message}"
    }
}
