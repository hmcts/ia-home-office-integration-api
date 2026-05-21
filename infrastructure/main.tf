provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

locals {
  aseName = "core-compute-${var.env}"

  local_env = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "aat" : "saat" : var.env
  local_ase = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "core-compute-aat" : "core-compute-saat" : local.aseName

  preview_vault_name     = "${var.raw_product}-aat"
  non_preview_vault_name = "${var.raw_product}-${var.env}"
  key_vault_name         = var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name

  s2sUrl = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"
}


resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location
  tags     = merge(var.common_tags, tomap({ "lastUpdated" = timestamp() }))
}

data "azurerm_key_vault" "ia_key_vault" {
  name                = local.key_vault_name
  resource_group_name = local.key_vault_name
}

data "azurerm_key_vault_secret" "apim_app_id" {
  name         = "apim-ia-home-office-app-id"
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}

data "azurerm_key_vault_secret" "apim_client_id" {
  name         = "apim-ia-home-office-client-id"
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}

data "azurerm_key_vault_secret" "tenant_id" {
  name         = "apim-ia-home-office-tenant-id"
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}
# region API (gateway)
data "azurerm_key_vault_secret" "s2s_client_secret" {
  name         = "gateway-s2s-client-secret"
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}

data "azurerm_key_vault_secret" "s2s_client_id" {
  name         = "gateway-s2s-client-id"
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}