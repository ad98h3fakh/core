import * as core from '@actions/core'
import * as fs from 'fs'
import * as integration from './integration'

const buildEnv = core.getInput('build_env')
const dbType = core.getInput('db_type')

/**
 * Main entry point for this action.
 */
const run = async () => {
  core.info("Running Core's integration tests")

  const cmd = integration.COMMANDS[buildEnv as keyof integration.Commands]
  if (!cmd) {
    core.error('Cannot resolve build tool, aborting')
    return
  }

  const exitCode = await integration.runTests(cmd)
  const skipReport = !(cmd.outputDir && fs.existsSync(cmd.outputDir))
  setOutput('tests_results_location', cmd.outputDir)
  setOutput('tests_results_report_location', cmd.reportDir)
  setOutput('ci_index', cmd.ciIndex)
  setOutput('tests_results_status', exitCode === 0 ? 'PASSED' : 'FAILED')
  setOutput('tests_results_skip_report', skipReport)
  setOutput(`${dbType}_tests_results_status`, exitCode === 0 ? 'PASSED' : 'FAILED')
  setOutput(`${dbType}_tests_results_skip_report`, skipReport)
}

const setOutput = (name: string, value: string | boolean | number | undefined) => {
  const val = value === undefined ? '' : value
  core.notice(`Setting output '${name}' with value: '${val}'`)
  core.setOutput(name, value)
}

// Run main function
run()
