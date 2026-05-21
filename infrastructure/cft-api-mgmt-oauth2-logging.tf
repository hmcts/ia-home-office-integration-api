resource "azurerm_api_management_api_diagnostic" "apim_ia_home_office_api_logs" {
  provider                 = azurerm.aks-cftapps
  identifier               = "applicationinsights"
  resource_group_name      = local.cft_api_mgmt_oauth2_rg
  api_management_name      = local.cft_api_mgmt_oauth2_name
  api_name                 = "ia-home-office-api"

  sampling_percentage       = 100.0
  always_log_errors         = true
  log_client_ip             = true
  verbosity                 = "verbose"
  http_correlation_protocol = "W3C"

  frontend_request {
    body_bytes = 8192
    headers_to_log = [
      "content-type",
      "accept",
      "origin"
    ]
  }

  frontend_response {
    body_bytes = 8192
    headers_to_log = [
      "content-type",
      "content-length",
      "origin"
    ]
  }

  backend_request {
    body_bytes = 8192
    headers_to_log = [
      "content-type",
      "accept",
      "origin"
    ]
  }

  backend_response {
    body_bytes = 8192
    headers_to_log = [
      "content-type",
      "content-length",
      "origin"
    ]
  }

  depends_on = [
    module.cft_api_mgmt_oauth2_api
  ]
}
