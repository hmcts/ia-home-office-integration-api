output "microserviceName" {
  value = "${var.component}"
}

output "resourceGroup" {
  value = "{azurerm_resource_group.rg.name}"
}

output "vaultName" {
  value = "{local.key_vault_name}"
}

output "vaultUri" {
  value = "{data.azurerm_key_vault.ia_key_vault.vault_uri}"
}
