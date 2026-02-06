package com.daveestar.alltheitems.enums;

public enum Permissions {
  ADMIN("alltheitems.admin");

  private final String _permission;

  Permissions(String permission) {
    _permission = permission;
  }

  public String getName() {
    return _permission;
  }
}
