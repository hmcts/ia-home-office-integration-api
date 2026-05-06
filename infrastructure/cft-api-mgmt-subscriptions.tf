# Subscription keys for the CFT APIM

# Supplier subscription - home office
resource "azurerm_api_management_subscription" "home_office_ia_subscription" {
  api_management_name = local.cft_api_mgmt_name
  resource_group_name = local.cft_api_mgmt_rg
  product_id          = module.cft_api_mgmt_product.id
  display_name        = "IA API - Home Office Subscription"
  state               = "active"
  provider            = azurerm.aks-cftapps
}

resource "azurerm_key_vault_secret" "home_office_ia_subscription_key" {
  name         = "home-office-ia-apim-cft-subscription-key"
  value        = azurerm_api_management_subscription.home_office_ia_subscription.primary_key
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}
