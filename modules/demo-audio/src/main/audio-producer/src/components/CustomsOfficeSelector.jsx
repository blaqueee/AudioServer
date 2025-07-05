"use client";

import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import CheckboxTree from 'react-checkbox-tree';
import 'react-checkbox-tree/lib/react-checkbox-tree.css';
import { fetchParents, fetchChildren } from '../lib/api';

import {
  MdCheckBox, MdCheckBoxOutlineBlank, MdOutlineIndeterminateCheckBox
} from 'react-icons/md'; // Чекбоксы
import { IoMdArrowDropright, IoMdArrowDropdown } from "react-icons/io";
import { FaRegFolder, FaRegFolderOpen, FaRegFile } from "react-icons/fa"; 

const mapToTreeNodes = (items) => {
  if (!items) return [];
  return items.map(item => ({
    value: item.id.toString(),
    label: item.name,
    children: [],
  }));
};

const CustomsOfficeSelector = React.memo(({ onSelectionChange }) => {
  const [nodes, setNodes] = useState([]);
  const [checked, setChecked] = useState([]);
  const [expanded, setExpanded] = useState([]);
  const [loadingNodes, setLoadingNodes] = useState(new Set());
  
  // Use ref to store current nodes to avoid dependency issues
  const nodesRef = useRef(nodes);
  nodesRef.current = nodes;

  const CustomIcons = useMemo(() => ({
    check: <MdCheckBox style={{ color: '#666cff' }} />,
    uncheck: <MdCheckBoxOutlineBlank style={{ color: '#aaa' }} />,
    halfCheck: <MdOutlineIndeterminateCheckBox style={{ color: '#666cff', opacity: 0.7 }} />, 

    expandClose: <IoMdArrowDropright style={{ color: '#e0e0e0', fontSize: '1.2em' }} />,
    expandOpen: <IoMdArrowDropdown style={{ color: '#e0e0e0', fontSize: '1.2em' }} />,
    expandAll: <IoMdArrowDropright style={{ color: '#e0e0e0', fontSize: '1.2em' }} />,
    collapseAll: <IoMdArrowDropdown style={{ color: '#e0e0e0', fontSize: '1.2em' }} />,

    parentClose: <FaRegFolder style={{ color: '#ffd700', marginRight: '5px' }} />, 
    parentOpen: <FaRegFolderOpen style={{ color: '#ffd700', marginRight: '5px' }} />, 
    leaf: <FaRegFile style={{ color: '#b0c4de', marginRight: '5px' }} />, 
  }), []);

  useEffect(() => {
    const loadInitialNodes = async () => {
      console.log("Loading initial parent nodes...");
      const parentOffices = await fetchParents();
      console.log("Initial parent offices fetched:", parentOffices);
      const mappedNodes = mapToTreeNodes(parentOffices);
      setNodes(mappedNodes);
    };
    loadInitialNodes();
  }, []);

  const updateNodeChildren = useCallback((currentNodes, targetId, newChildrenData) => {
    return currentNodes.map(node => {
      if (node.value === targetId) {
        return {
          ...node,
          children: mapToTreeNodes(newChildrenData), 
        };
      }
      if (node.children && node.children.length > 0) {
        return {
          ...node,
          children: updateNodeChildren(node.children, targetId, newChildrenData), 
        };
      }
      return node;
    });
  }, []);

  const findNode = useCallback((currentNodes, id) => {
    for (const node of currentNodes) {
      if (node.value === id) return node;
      if (node.children && node.children.length > 0) {
        const found = findNode(node.children, id);
        if (found) return found;
      }
    }
    return null;
  }, []);

  const onExpand = useCallback(async (newExpanded, nodeObject) => {
    setExpanded(newExpanded);

    const expandedNodeId = nodeObject.value || (nodeObject.node ? nodeObject.node.value : null);
    const isExpanded = nodeObject.isExpanded !== undefined ? nodeObject.isExpanded : (newExpanded.includes(expandedNodeId));

    console.log("onExpand triggered:", { newExpanded, expandedNodeId, isExpanded });

    if (isExpanded && expandedNodeId && !loadingNodes.has(expandedNodeId)) {
      setLoadingNodes(prev => new Set(prev).add(expandedNodeId));
      
      try {
        // Use ref to get current nodes instead of dependency
        const currentNodes = nodesRef.current;
        const nodeToExpand = findNode(currentNodes, expandedNodeId);

        if (nodeToExpand && nodeToExpand.children && nodeToExpand.children.length === 0) {
          console.log(`Fetching children for node ID: ${expandedNodeId}`);
          const children = await fetchChildren(expandedNodeId);
          console.log(`Children fetched for ${expandedNodeId}:`, children);

          if (children && Array.isArray(children) && children.length > 0) {
            setNodes(prevNodes => updateNodeChildren(prevNodes, expandedNodeId, children));
          } else {
            console.log(`No children or invalid data for ${expandedNodeId}. Mark as leaf if needed.`);
          }
        } else {
          console.log(`Node ${expandedNodeId} already has children or is a leaf, skipping fetch.`);
        }
      } finally {
        setLoadingNodes(prev => {
          const newSet = new Set(prev);
          newSet.delete(expandedNodeId);
          return newSet;
        });
      }
    }
  }, [updateNodeChildren, findNode, loadingNodes]); // Removed 'nodes' from dependencies

  useEffect(() => {
    onSelectionChange(checked);
  }, [checked, onSelectionChange]);

  const treeContainerStyle = useMemo(() => ({
    border: '1px solid #4a4d6b',
    padding: '10px',
    minHeight: '200px',
    maxHeight: '400px',
    overflowY: 'auto',
    borderRadius: '8px',
    backgroundColor: '#2b2e45',
  }), []);

  return (
    <div style={{
      marginBottom: '30px',
    }}>
      <h3 style={{ color: '#666cff', marginBottom: '15px' }}>Выбор таможенных офисов</h3>
      <div style={treeContainerStyle}>
        {nodes.length > 0 ? (
          <CheckboxTree
            nodes={nodes}
            checked={checked}
            expanded={expanded}
            onCheck={setChecked}
            onExpand={onExpand}
            icons={CustomIcons}
            showNodeTitles={true}
            noResultsText="Офисы не найдены"
            noCascade={true}
          />
        ) : (
          <p style={{ color: '#e0e0e0', textAlign: 'center', padding: '20px' }}>Загрузка таможенных офисов...</p>
        )}
      </div>
      <p style={{ marginTop: '15px', fontSize: '0.9em', color: '#e0e0e0' }}>Выбрано ID: {checked.join(', ')}</p>
    </div>
  );
});

CustomsOfficeSelector.displayName = 'CustomsOfficeSelector';

export default CustomsOfficeSelector;