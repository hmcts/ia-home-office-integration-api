provider "azurerm" {
  features {}
  alias           = "aks-cftapps"
  subscription_id = var.aks_subscription_id
}
