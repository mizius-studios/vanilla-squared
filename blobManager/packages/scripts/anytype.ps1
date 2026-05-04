$ErrorActionPreference = "Stop"
$Version = "1.0.0"
$PackageName = "anytype"

$errorApiModule = Import-Module (Join-Path $PSScriptRoot "..\util\api\errorAPI.psm1") -Force -DisableNameChecking -PassThru
$configApiModule = Import-Module (Join-Path $PSScriptRoot "..\util\api\configAPI.psm1") -Force -DisableNameChecking -PassThru
$networkApiModule = Import-Module (Join-Path $PSScriptRoot "..\util\api\networkAPI.psm1") -Force -DisableNameChecking -PassThru
$keyApiModule = Import-Module (Join-Path $PSScriptRoot "..\util\api\keyAPI.psm1") -Force -DisableNameChecking -PassThru
$versionModule = Import-Module (Join-Path $PSScriptRoot "..\util\api\commands\version.psm1") -Force -DisableNameChecking -PassThru
$helpModule = Import-Module (Join-Path $PSScriptRoot "..\util\api\commands\help.psm1") -Force -DisableNameChecking -PassThru
$pingModule = Import-Module (Join-Path $PSScriptRoot "..\util\api\commands\ping.psm1") -Force -DisableNameChecking -PassThru

$Cmd = @{
    NewMessageState        = $errorApiModule.ExportedCommands["New-MessageState"]
    AddErrorMessage        = $errorApiModule.ExportedCommands["Add-ErrorMessage"]
    ThrowIfErrors          = $errorApiModule.ExportedCommands["Throw-IfErrors"]
    WriteWarnings          = $errorApiModule.ExportedCommands["Write-Warnings"]
    GetJsonConfig          = $configApiModule.ExportedCommands["Get-JsonConfig"]
    GetConfigValue         = $configApiModule.ExportedCommands["Get-ConfigValue"]
    TestConfigVersion      = $configApiModule.ExportedCommands["Test-ConfigVersion"]
    InvokeNetworkRequest   = $networkApiModule.ExportedCommands["Invoke-NetworkRequest"]
    MergeAuthorizationHeader = $networkApiModule.ExportedCommands["Merge-AuthorizationHeader"]
    JoinApiUri             = $networkApiModule.ExportedCommands["Join-ApiUri"]
    ResolveKeyValue        = $keyApiModule.ExportedCommands["Resolve-KeyValue"]
    CommandGetKey          = $keyApiModule.ExportedCommands["Command-GetKey"]
    CommandVersion         = $versionModule.ExportedCommands["Command-Version"]
    CommandHelp            = $helpModule.ExportedCommands["Command-Help"]
    CommandPing            = $pingModule.ExportedCommands["Command-Ping"]
}

foreach ($entry in $Cmd.GetEnumerator()) {
    if ($null -eq $entry.Value) {
        throw "Required command handle missing: $($entry.Key)"
    }
}

$configPath = Join-Path $PSScriptRoot "..\config\anytype.json"
$config = & $Cmd.GetJsonConfig -Path $configPath -Fallback @{}
$networkConfig = & $Cmd.GetConfigValue -Config $config -Key "network" -DefaultValue @{}
$headers = & $Cmd.GetConfigValue -Config $networkConfig -Key "headers" -DefaultValue @{}
$baseUrl = & $Cmd.GetConfigValue -Config $networkConfig -Key "baseUrl" -DefaultValue "http://localhost:31009"
$apiVersion = & $Cmd.GetConfigValue -Config $networkConfig -Key "apiVersion" -DefaultValue "v1"
$connectionTestUrl = & $Cmd.GetConfigValue -Config $networkConfig -Key "connectionTestUrl" -DefaultValue "http://localhost:31009/v1/spaces"
$authenticatedTestEndpoint = & $Cmd.GetConfigValue -Config $networkConfig -Key "authenticatedTestEndpoint" -DefaultValue "/spaces"
$timeoutSeconds = & $Cmd.GetConfigValue -Config $networkConfig -Key "timeoutSeconds" -DefaultValue 30
$authConfig = & $Cmd.GetConfigValue -Config $config -Key "auth" -DefaultValue @{}
$authKeyFile = & $Cmd.GetConfigValue -Config $authConfig -Key "keyFile" -DefaultValue "blobManager/packages/config/keys/personal.txt"
$authKeyName = & $Cmd.GetConfigValue -Config $authConfig -Key "keyName" -DefaultValue "AnytypeApiKey"
$authEnvVar = & $Cmd.GetConfigValue -Config $authConfig -Key "envVar" -DefaultValue "ANYTYPE_API_KEY"

$state = & $Cmd.NewMessageState
$versionStatus = & $Cmd.TestConfigVersion -PackageName $PackageName -ScriptVersion $Version -Config $config -State $state

function Resolve-LocalPath {
    param(
        [string]$PathValue
    )

    if ([string]::IsNullOrWhiteSpace([string]$PathValue)) {
        return $null
    }

    if ([System.IO.Path]::IsPathRooted($PathValue)) {
        return $PathValue
    }

    return [System.IO.Path]::GetFullPath((Join-Path (Get-Location) $PathValue))
}

function Add-RequiredConfigError {
    param(
        [string]$Value,
        [string]$ConfigKey
    )

    if ([string]::IsNullOrWhiteSpace([string]$Value)) {
        & $Cmd.AddErrorMessage -State $state -Message "anytype $ConfigKey is missing in blobManager\\packages\\config\\anytype.json"
    }
}

function Get-AnytypeAuthErrorMessage {
    param(
        $Result
    )

    if ($null -eq $Result) {
        return "Authenticated request failed."
    }

    if ($null -eq $Result.StatusCode) {
        if (-not [string]::IsNullOrWhiteSpace([string]$Result.ErrorMessage)) {
            return "Anytype API/network failure: $($Result.ErrorMessage)"
        }

        return "Anytype API/network failure."
    }

    if ($Result.StatusCode -eq 401) {
        return "Unauthorized. Check that $authKeyName is set in the key file or $authEnvVar and that the key is still valid."
    }

    $detail = $null
    if (-not [string]::IsNullOrWhiteSpace([string]$Result.RawContent)) {
        $detail = [string]$Result.RawContent
    }

    if (-not [string]::IsNullOrWhiteSpace([string]$detail)) {
        return "Anytype API failure: $($Result.StatusCode) $($Result.StatusDescription) - $detail"
    }

    return "Anytype API failure: $($Result.StatusCode) $($Result.StatusDescription)"
}

function Get-AnytypeDisplayName {
    param(
        $Data
    )

    if ($null -eq $Data) {
        return $null
    }

    if ($Data -is [System.Collections.IEnumerable] -and $Data -isnot [string]) {
        foreach ($item in $Data) {
            $candidate = Get-AnytypeDisplayName -Data $item
            if (-not [string]::IsNullOrWhiteSpace([string]$candidate)) {
                return $candidate
            }
        }

        return $null
    }

    foreach ($propertyName in @("name", "spaceName", "id", "targetSpaceId")) {
        if ($Data.PSObject.Properties.Name -contains $propertyName) {
            $value = [string]$Data.$propertyName
            if (-not [string]::IsNullOrWhiteSpace($value)) {
                return $value
            }
        }
    }

    foreach ($nestedName in @("spaces", "data", "list")) {
        if ($Data.PSObject.Properties.Name -contains $nestedName) {
            $candidate = Get-AnytypeDisplayName -Data $Data.$nestedName
            if (-not [string]::IsNullOrWhiteSpace([string]$candidate)) {
                return $candidate
            }
        }
    }

    return $null
}

$Command = if ($args.Count -gt 0) { $args[0] } else { $null }

switch ($Command) {
    "-p" {
        Add-RequiredConfigError -Value $baseUrl -ConfigKey "network.baseUrl"
        Add-RequiredConfigError -Value $apiVersion -ConfigKey "network.apiVersion"
        Add-RequiredConfigError -Value $connectionTestUrl -ConfigKey "network.connectionTestUrl"
        & $Cmd.ThrowIfErrors -State $state

        Write-Host "Testing Anytype local API for package '$PackageName'..."
        Write-Host "Resolved API route: $connectionTestUrl"
        $result = & $Cmd.InvokeNetworkRequest -Uri $connectionTestUrl -Method "GET" -Headers $headers -TimeoutSeconds $timeoutSeconds

        if ($result.Success) {
            Write-Host "Connection successful." -ForegroundColor Green
            Write-Host "Status: $($result.StatusCode) $($result.StatusDescription)"
            & $Cmd.WriteWarnings -State $state
            exit 0
        }

        if ($result.StatusCode -eq 401) {
            Write-Host "Service reachable. Endpoint requires authentication, which is expected for /v1/spaces." -ForegroundColor Green
            Write-Host "Status: $($result.StatusCode) $($result.StatusDescription)"
            & $Cmd.WriteWarnings -State $state
            exit 0
        }

        Write-Host "Connection failed." -ForegroundColor Red
        if ($null -ne $result.StatusCode) {
            Write-Host "Status: $($result.StatusCode) $($result.StatusDescription)" -ForegroundColor Red
        }
        if (-not [string]::IsNullOrWhiteSpace([string]$result.ErrorMessage)) {
            Write-Host "Error: $($result.ErrorMessage)" -ForegroundColor Red
        }
        if (-not [string]::IsNullOrWhiteSpace([string]$result.RawContent)) {
            Write-Host "Response: $($result.RawContent)" -ForegroundColor DarkYellow
        }
        & $Cmd.WriteWarnings -State $state
        exit 1
    }

    "-getAuth" {
        $keyFilePath = Resolve-LocalPath -PathValue $authKeyFile
        $result = & $Cmd.CommandGetKey -PackageName $PackageName -KeyFilePath $keyFilePath -KeyName $authKeyName -EnvVarName $authEnvVar -ExpectedFormatExample "your_api_key"
        if ($result.Success) {
            exit 0
        }

        exit 1
    }

    "-auth" {
        Add-RequiredConfigError -Value $baseUrl -ConfigKey "network.baseUrl"
        Add-RequiredConfigError -Value $apiVersion -ConfigKey "network.apiVersion"
        Add-RequiredConfigError -Value $authenticatedTestEndpoint -ConfigKey "network.authenticatedTestEndpoint"
        Add-RequiredConfigError -Value $authKeyFile -ConfigKey "auth.keyFile"
        Add-RequiredConfigError -Value $authKeyName -ConfigKey "auth.keyName"
        Add-RequiredConfigError -Value $authEnvVar -ConfigKey "auth.envVar"
        & $Cmd.ThrowIfErrors -State $state

        $keyFilePath = Resolve-LocalPath -PathValue $authKeyFile
        $tokenResolution = & $Cmd.ResolveKeyValue -KeyFilePath $keyFilePath -KeyName $authKeyName -EnvVarName $authEnvVar
        $requestUri = & $Cmd.JoinApiUri -BaseUrl $baseUrl -ApiVersion $apiVersion -Endpoint $authenticatedTestEndpoint
        if (-not $tokenResolution.Success) {
            Write-Host "Missing Anytype API key. No local key or env var was found." -ForegroundColor Red
            Write-Host "Resolved API route: $requestUri"
            Write-Host "Expected local file: $keyFilePath" -ForegroundColor Red
            Write-Host "Expected file format: $authKeyName=<your_api_key>" -ForegroundColor DarkYellow
            Write-Host "Fallback env var: $authEnvVar" -ForegroundColor DarkYellow
            exit 1
        }

        $authHeaders = & $Cmd.MergeAuthorizationHeader -Headers $headers -Token "Bearer $($tokenResolution.Value)"
        Write-Host "Resolved API route: $requestUri"
        Write-Host "Auth source: $($tokenResolution.Source)"

        $result = & $Cmd.InvokeNetworkRequest -Uri $requestUri -Method "GET" -Headers $authHeaders -TimeoutSeconds $timeoutSeconds
        if ($result.Success -and $result.StatusCode -eq 200) {
            $displayName = Get-AnytypeDisplayName -Data $result.Data

            Write-Host "Authenticated request successful." -ForegroundColor Green
            Write-Host "Status: $($result.StatusCode) $($result.StatusDescription)"
            if (-not [string]::IsNullOrWhiteSpace([string]$displayName)) {
                Write-Host "Space: $displayName"
            }
            & $Cmd.WriteWarnings -State $state
            exit 0
        }

        Write-Host "Authenticated request failed." -ForegroundColor Red
        if ($null -ne $result.StatusCode) {
            Write-Host "Status: $($result.StatusCode) $($result.StatusDescription)" -ForegroundColor Red
        }

        $authError = Get-AnytypeAuthErrorMessage -Result $result
        if (-not [string]::IsNullOrWhiteSpace([string]$authError)) {
            Write-Host $authError -ForegroundColor Red
        }

        if (-not [string]::IsNullOrWhiteSpace([string]$result.RawContent)) {
            Write-Host "Response: $($result.RawContent)" -ForegroundColor DarkYellow
        }

        exit 1
    }

    "-v" {
        & $Cmd.CommandVersion -PackageName $PackageName -Version $Version -ConfigVersion $versionStatus.ConfigVersion -WarningMessage $versionStatus.WarningMessage
        exit 0
    }

    "-?" {
        & $Cmd.WriteWarnings -State $state
        & $Cmd.CommandHelp -PackageName $PackageName -Commands @("anytype -p", "anytype -auth", "anytype -getAuth", "anytype -v", "anytype -?")
        Write-Host "Auth key file: $authKeyFile"
        Write-Host "Auth key format: $authKeyName=<your_api_key>"
        Write-Host "Automation env var: $authEnvVar"
        exit 0
    }

    default {
        & $Cmd.WriteWarnings -State $state
        Write-Host "Unknown anytype command: $Command"
        Write-Host "Run: .\blob.ps1 anytype -?"
        exit 1
    }
}
