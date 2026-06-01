# Subscription keys for the CFT APIM

# home office subscription
resource "azurerm_api_management_subscription" "home_office_stf_subscription" {
  api_management_name = local.cft_api_mgmt_oauth2_name
  resource_group_name = local.cft_api_mgmt_oauth2_rg
  product_id          = module.cft_api_mgmt_oauth2_product.id
  display_name        = "Home Office Stf Status Subscription"
  state               = "active"
  provider            = azurerm.aks-cftapps
}

resource "azurerm_key_vault_secret" "home_office_stf_subscription_key" {
  name         = "ia-home-office-stf-cft-apim-subscription-key"
  value        = azurerm_api_management_subscription.home_office_stf_subscription.primary_key
  key_vault_id = data.azurerm_key_vault.ia_key_vault.id
}


