package com.metaformsystems.redline.dao;

import java.util.List;

/**
 *
 */
public record DataspaceInfo(Long id, Long dataspaceId,  List<String> agreementTypes, List<String> roles) {

}
