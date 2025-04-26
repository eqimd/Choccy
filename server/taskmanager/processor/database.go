package processor

import (
	"choccy/server/database/model"
	"choccy/server/util"
)

// CheckDatabaseUpdates 返回新版本名，下载地址
func CheckDatabaseUpdates(task *model.Task, project *model.Project) (string, string) {
	codeQLDatabase, err := util.GetGithubDatabase(task.ProjectOwner, task.ProjectRepo, task.ProjectLanguage)
	if err != nil {
		panic("Failed to obtain the database: " + err.Error())
	}
	SetProjectLatestVersion(project, codeQLDatabase.CommitOid, codeQLDatabase.CreatedAt)
	return codeQLDatabase.CommitOid, codeQLDatabase.Url
}

func DownloadDatabase(task *model.Task, url string, commit string, databaseName string) string {
	WriteTaskLog(task, "Download version: "+commit)
	databasePath, err := util.DownloadGithubDatabase(url, databaseName)
	if err != nil {
		panic("Database download failed: " + err.Error())
	}
	WriteTaskLog(task, "The download was successful, the path: "+databasePath)
	return databasePath
}
