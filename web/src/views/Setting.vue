<template>
  <el-tabs type="border-card">
    <el-tab-pane label="Environment">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="box-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-tooltip
                    content="Set the absolute path of each CodeQL configuration or the relative path relative to the Chocolate binary"
                    placement="right"
                    :hide-after="10"
                >
                  <span>CodeQL</span>
                </el-tooltip>
              </div>
            </template>
            <el-form label-position="left" label-width="80px">
              <el-form-item label="Cli">
                <template #label>
                  <el-tooltip
                      raw-content
                      content="CodeQL engine binary path, download address：https://github.com/github/codeql-cli-binaries/releases<br>If set to codeql, it will be looked for from the environment variable"
                      placement="top"
                  >
                    <span>Cli</span>
                  </el-tooltip>
                </template>
                <el-input v-model="setting.CodeQLCli" @change="(value) => settingOnchange('CodeQLCli',value)">
                  <template #append>
                    <span>{{ state.CodeQLCli_ver }}</span>
                  </template>
                </el-input>
              </el-form-item>


              <el-form-item label="Lib">
                <template #label>
                  <el-tooltip
                      raw-content
                      content="CodeQL library path, download address：https://github.com/github/codeql/tags<br>Whether it is set or not, codeql will automatically look for it according to certain rules."
                      placement="top"
                  >
                    <span>Lib</span>
                  </el-tooltip>
                </template>
                <el-input v-model="setting.CodeQLLib" @change="saveData">
                </el-input>
              </el-form-item>

              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content="Customize the list of package paths, split by line breaks<br>Whether it is set or not, codeql will automatically look for it according to certain rules."
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Packs</span>
                  </el-tooltip>
                </template>
                <el-input v-model="setting.CodeQLPacks"
                          @change="saveData"
                          :autosize="{ minRows: 1, maxRows: 5 }"
                          type="textarea">
                </el-input>
              </el-form-item>

              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content="Query package path"
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Suite</span>
                  </el-tooltip>
                </template>
                <el-input v-model="setting.CodeQLSuite"
                          @change="saveData">
                </el-input>
              </el-form-item>

              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content="CodeQL database storage path"
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Database</span>
                  </el-tooltip>
                </template>
                <el-input v-model="setting.CodeQLDatabase" @change="saveData">
                </el-input>
              </el-form-item>

              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content="CodeQL analysis result storage path"
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Result</span>
                  </el-tooltip>
                </template>
                <el-input v-model="setting.CodeQLResult" @change="saveData">
                </el-input>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="box-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-tooltip
                    content="Set the environment variables when command-line tools such as CodeQL are running, such as Path variables such as Java, Go, and Maven, network agents, etc.<br>Inherited from system environment variables by default, and can be referenced and overwritten. Reference variable syntax:${variable name}"
                    raw-content
                    placement="right"
                    :hide-after="10"
                >
                  <span>Environment variables</span>
                </el-tooltip>
              </div>
            </template>
            <el-input
                v-model="setting.EnvStr"
                :autosize="{ minRows: 13.8, maxRows: 13.8 }"
                type="textarea"
                @change="(value) => settingOnchange('EnvStr',value)"
            />
          </el-card>
        </el-col>
      </el-row>
      <el-card class="box-card" shadow="never" style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>Environment variables</span>
          </div>
        </template>
        <el-descriptions
            style="margin-top: 10px;"
            :column="1"
            border
            size="small"
        >
          <el-descriptions-item
              v-for="(val, key, index) in state.Env"
              :label="key.toString()"
              class-name="my-class"
              label-class-name="my-label"
          >
            {{ val }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-tab-pane>


    <el-tab-pane label="Other">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="box-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>Credentials</span>
              </div>
            </template>
            <el-form label-position="left" label-width="110px">
              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content="The password used to access the system<br>This system has potentially arbitrary command execution and file reading functions, please be sure to set a strong password"
                      placement="top"
                      :hide-after="10"
                  >
                    <span>System authentication token</span>
                  </el-tooltip>
                </template>
                <el-input v-model="setting.SystemToken" @change="saveData"></el-input>
              </el-form-item>
              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content="Used for version detection, source code and database download, private warehouse code scanning, etc., must be set<br>Get address: https://github.com/settings/tokens"
                      placement="top"
                  >
                    <span>GitHub Token</span>
                  </el-tooltip>
                </template>
                <el-input v-model="setting.GithubToken" @change="saveData"/>
              </el-form-item>
            </el-form>
          </el-card>
          <el-card class="box-card" shadow="never" style="margin-top: 20px">
            <template #header>
              <div class="card-header">
                <span>Scan</span>
              </div>
            </template>
            <el-form label-position="left" label-width="170px">
              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content='When scanning a release for the first time, how many release versions of the latest release need to be scanned (minimum 1, maximum 10)'
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Number of releases scanned for the first time</span>
                  </el-tooltip>
                </template>
                <el-input type="number" v-model.number="setting.FirstReleaseCount" @change="saveData">
                </el-input>
              </el-form-item>
              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content='30 3-6,20-23 * * * (Minutes Hours DayOfMonth Month DayOfWeek)<br>@yearly @monthly @weekly @daily @hourly<br>@every 1h30m10s<br>Expression document：https://pkg.go.dev/github.com/robfig/cron/v3#hdr-CRON_Expression_Format'
                      placement="top"
                  >
                    <span>Scan Cron expressions regularly</span>
                  </el-tooltip>
                </template>
                <el-input v-model="setting.CronTaskSpec" @change="saveData">
                  <template #append>
                    <el-tooltip
                        content='Next execution time'
                        placement="top"
                        :hide-after="10"
                    >{{ timeFormatter(setting.CronTaskNextTime) }}
                    </el-tooltip>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content='When analyzing the database, additional command-line options are required<br>Reference：https://docs.github.com/en/code-security/codeql-cli/codeql-cli-manual/database-analyze#options'
                      placement="top"
                  >
                    <span>CodeQL additional command line options</span>
                  </el-tooltip>
                </template>
                <el-input v-model="setting.CodeQLAnalyzeOptions" @change="saveData"/>
              </el-form-item>

              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content='When the program ends and re-runs, does it resume execution and tasks in the queue?'
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Automatic task recovery</span>
                  </el-tooltip>
                </template>
                <el-switch v-model="setting.AutoRecoveryTask" @change="saveData"/>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="box-card" shadow="never" >
            <template #header>
              <div class="card-header">
                <span>System</span>
              </div>
            </template>
            <el-form label-position="left" label-width="165px">
              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content='When visiting the "GitHub Project" page, get the minimum time interval between "Latest Version" and "Update Time" (does not affect the update detection in the task)<br>The unit is minutes. If set to 0, it will be obtained in real time every time the page is refreshed.'
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Project page update detection interval</span>
                  </el-tooltip>
                </template>
                <el-input type="number" v-model.number="setting.UpdateDetectionInterval" @change="saveData">
                </el-input>
              </el-form-item>
              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content='Whether to ignore HTTPS certificate verification when initiating various HTTPS requests<br>(For debugging, do not turn on this option under normal circumstances)'
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Ignore HTTPS certificate verification</span>
                  </el-tooltip>
                </template>
                <el-switch v-model="setting.SkipVerifyTLS" @change="saveData"/>
              </el-form-item>
            </el-form>
          </el-card>
          <el-card class="box-card" shadow="never" style="margin-top: 20px">
            <template #header>
              <div class="card-header">
                <span>Automatically read</span>
              </div>
            </template>
            <el-form label-position="left" label-width="170px">
              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content='Tasks whose automatic read status is completed and no scanned content are automatically read'
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Automatically read tasks without scanned items</span>
                  </el-tooltip>
                </template>
                <el-switch v-model="setting.AutoReadEmptyTask" @change="saveData"/>
              </el-form-item>

              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content='Tasks whose automatic read status is completed and the number of results is 0 are automatically read'
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Automatically read fruitless tasks</span>
                  </el-tooltip>
                </template>
                <el-switch v-model="setting.AutoReadNoResultTask" @change="saveData"/>
              </el-form-item>

              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content='Automatically read tasks with completed status'
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Automatically read normally completed tasks</span>
                  </el-tooltip>
                </template>
                <el-switch v-model="setting.AutoReadCompletedTask" @change="saveData"/>
              </el-form-item>

              <el-form-item label="">
                <template #label>
                  <el-tooltip
                      raw-content
                      content='Automatically scan results with a number of read results of 0'
                      placement="top"
                      :hide-after="10"
                  >
                    <span>Automatically read the fruitless Sarif</span>
                  </el-tooltip>
                </template>
                <el-switch v-model="setting.AutoReadNoResultResult" @change="saveData"/>
              </el-form-item>

            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </el-tab-pane>
  </el-tabs>
</template>

<style>
.el-card__header {
  padding-top: 10px;
  padding-bottom: 10px;
}

.my-label {
  width: 100px;
  color: #999;
  font-weight: normal;
  background: #fff;
}

.my-class {
  max-width: 295px;
  word-break: break-all;
  word-wrap: break-word;
}
</style>

<script lang="ts" setup>
import {onMounted, reactive, ref} from "vue";
import {getSetting, saveSetting, testSetting} from '../api/setting.js'
import {ElMessage} from "element-plus";
import {setToken} from "../utils/auth.js"
import {timeFormatter} from '../utils/formatter.js'


const emit = defineEmits(["refresh"]);

const state = reactive({
  CodeQLCli_ver: '',
  Env: ''
})

const setting = ref({
  CodeQLCli: '',
  CodeQLLib: '',
  CodeQLPacks: '',
  CodeQLSuite: '',
  CodeQLDatabase: '',
  CodeQLResult: '',
  EnvStr: '',

  SystemToken: '',
  GithubToken: '',


  UpdateDetectionInterval: 0,
  SkipVerifyTLS: false,

  AutoRecoveryTask: false,
  FirstReleaseCount: 0,
  CronTaskSpec: '',
  CronTaskNextTime: '',

  AutoReadEmptyTask: false,
  AutoReadNoResultTask: false,
  AutoReadCompletedTask: false,
  AutoReadNoResultResult: false,

  CodeQLAnalyzeOptions:''
})

const settingOnchange = (key, value) => {
  getSettingTest(key, value).then(_ => {
    saveData()
  })
}

const getSettingTest = (key, value) => {
  return testSetting(key, value).then(response => {
    if (key == "CodeQLCli") {
      state.CodeQLCli_ver = response.data
    } else if (key == "EnvStr") {
      state.Env = response.data
    }
  })
}

const saveData = () => {
  saveSetting(setting.value).then(response => {
    ElMessage.success("Saved successfully")
    setToken(setting.value.SystemToken)
    fetchData()
  })
}

const fetchData = () => {
  return getSetting().then(response => {
    setting.value = response.data;
  })
}

onMounted(() => {
  fetchData().then(_ => {
    getSettingTest("CodeQLCli", setting.value.CodeQLCli);
    getSettingTest("EnvStr", setting.value.EnvStr)
  })
})
</script>
