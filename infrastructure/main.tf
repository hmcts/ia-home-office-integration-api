provider "azurerm" {
  version = "1.27.1"
}

locals {

  preview_vault_name           = "${var.raw_product}-aat"
  non_preview_vault_name       = "${var.raw_product}-${var.env}"
  key_vault_name               = var.env == "preview" || var.env == "spreview" ? local.preview_vault_name : local.non_preview_vault_name

}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location
  tags     = merge(var.common_tags, map("lastUpdated", timestamp()))
}

data "azurerm_key_vault" "ia_key_vault" {
  name                = local.key_vault_name
  resource_group_name = local.key_vault_name
}

data "azurerm_key_vault_secret" "idam_client_id" {
  name      = "idam-client-id"
  vault_uri = data.azurerm_key_vault.ia_key_vault.vault_uri
}

data "azurerm_key_vault_secret" "idam_secret" {
  name      = "idam-secret"
  vault_uri = data.azurerm_key_vault.ia_key_vault.vault_uri
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name      = "s2s-secret"
  vault_uri = data.azurerm_key_vault.ia_key_vault.vault_uri
}

data "azurerm_key_vault_secret" "s2s_microservice" {
  name      = "s2s-microservice"
  vault_uri = data.azurerm_key_vault.ia_key_vault.vault_uri
}