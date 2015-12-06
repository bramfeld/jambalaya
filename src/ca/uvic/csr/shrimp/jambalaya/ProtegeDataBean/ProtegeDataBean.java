/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.Icon;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyManager;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.OWLIconProviderImpl;
import org.protege.editor.owl.ui.editor.OWLClassExpressionSetEditor;
import org.protege.editor.owl.ui.editor.OWLIndividualEditor;
import org.protege.editor.owl.ui.editor.OWLDataPropertyEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyEditor;
import org.protege.editor.owl.ui.editor.OWLAnnotationEditor;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.DataBean.AbstractDataBean;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.ShrimpApplication.AbstractShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.util.CollectionUtils;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.jambalaya.JambalayaView;

/**
 * Protege Data Bean: This is a storage unit for the Protege-Shrimp system. It
 * is designed to hold and maintain the data structure being used by the system.
 *
 * @author Casey Best, Rob Lintern, Nasir Rather
 */
public class ProtegeDataBean extends AbstractDataBean implements Serializable {

    protected static final String UNKNOWN_ART_TYPE = "unknown";
    public static final String INSTANCE_ART_TYPE = "Individual";
    public static final String CLASS_ART_TYPE = "Class";
    public static final String DIRECT_SUBCLASS_SLOT_TYPE = "has subclass";
    public static final String DIRECT_INSTANCE_SLOT_TYPE = "has individual";
    public static final String SUB_CLASS_SOME_VALUE_OF = "(Subclass some)";
    public static final String SUB_CLASS_ALL_VALUES = "(Subclass all)";
    public static final String EQUIVALENT_CLASS_SOME_VALUE_OF = "(Equivalent class some)";
    public static final String EQUIVALENT_CLASS_ALL_VALUES = "(Equivalent class all)";

    public final static String RESTRICTION_TYPE = "Restriction";
    public final static String LOGICAL_OPERATION_TYPE = "Logical Operation";
    public final static String PRIMITIVE_CLASS_TYPE = "Primitive Class";
    public final static String DEFINED_CLASS_TYPE = "Defined Class";
    public final static String INDIVIDUAL_TYPE = "Individual";
    public final static String ENUMERATION_CLASS_TYPE = "Enumeration";
    public final static String RDFS_CLASS_TYPE = "RDFS Class";

    public static final String GROUP_NAME_CLASS_INSTANCE_HIERARCHY = "Class-Individual Hierarchy";
    public static final String GROUP_NAME_SLOT_INSTANCE = "Slots Connecting Individuals";
    //public static final String GROUP_NAME_SLOT_TEMPLATE_VALUE = "(Default Value) Group";
    public static final String GROUP_NAME_SLOT_TEMPLATE_ALLOWED_CLASS = "Slots Connecting Classes";
    public static final String GROUP_NAME_SYSTEM = "System";
    public static final String GROUP_NAME_REIFIED_RELS = "Direct Binary Relationships";

    public final static String GROUP_NAME_CONNECTING_INDIVIDUALS = "Properties On Individuals";
    public final static String GROUP_NAME_PROPERTY_RESTRICTIONS = "Property Restrictions On Classes";
    public final static String GROUP_NAME_LOGICAL_DESCRIPTION = "Logical Descriptions";
    public final static String GROUP_NAME_DOMAIN_RANGE = "Domain>Range";
    public static final String GROUP_NAME_INHERITED = "Inherited";

    public static final String SUFFIX_SLOT_INSTANCE = "";
    public static final String SUFFIX_SLOT_REIFIED_REL = "";
    public static final String SUFFIX_SLOT_TEMPLATE_ALLOWED_CLASS = " (Connects Classes)";
    public static final String SUFFIX_SLOT_TEMPLATE_VALUE = " (Default Value)";
	 
    public final static String SUFFIX_CONNECTS_INDIVIDUALS = "";
    public final static String SUFFIX_DOMAIN_RANGE = " (Domain>Range)";
    //private final static String SUFFIX_RESTRICTION = " (Restriction)";
    public final static String SUFFIX_INHERITED = " (Inherited)";
	 
    protected static final String[] SUBCLASS_OF_HIERARCHY = new String[] { DIRECT_SUBCLASS_SLOT_TYPE };
    protected static final String[] INSTANCE_OF_HIERARCHY = new String[] { DIRECT_INSTANCE_SLOT_TYPE };
    protected static final String[] SUBCLASS_OF_AND_INSTANCE_OF_HIERARCHY = new String[] { DIRECT_SUBCLASS_SLOT_TYPE, DIRECT_INSTANCE_SLOT_TYPE };
    // whether or not to consider the instances (potentially thousands) of metaclasses
    //protected static boolean CONSIDER_METACLASS_INSTANCES = true;
    //protected static String ATTR_FRAME_CREATION_TIMESTAMP = "frame creation/ timestamp";
    //protected static String ATTR_FRAME_LAST_MODIFICATION_TIMESTAMP = "frame last modification timestamp";
    //protected static String ATTR_FRAME_LAST_MODIFIER = "frame last modifier";
    //protected static String ATTR_FRAME_CREATOR = "frame creator";
 
    protected OWLModelManager modelManager = null;
    protected OWLEditorKit editorKit = null;
    protected OWLOntology ontology = null;

    /**
     * A set of root clses. These root classes, and all subclasses and instances
     * of these root classes, are considered the "working" set of this databean.
     */
    private Set<OWLEntity> rootEntities;

    /**
     * A set of super classes and super types of the chosen root classes. These
     * are used for determining whether or not a slot should be imported into
     * shrimp
     */
    protected Set validSlotSuperClsesAndTypes;

    private ProtegeMetrics metrics;

    /** Holds the widget associated with each frame, key is the Protege Frame. */
    private Map clsWidgets;

    /** Maps a protege frame to its text. */
    private Map frameToBrowserText;

    protected Map frameToArtifactTypeMap;

    protected Map relTypeToGroupMap;

    /** set of domain/range constraints for the OWL file */
    private Set<Relationship> domainRangeRelsBuffer = null;
	
    /** maps for caching reified relationships */
    private Map<Artifact, Set<Relationship>> incomingUnreifiedRels = null;
    private Map<Artifact, Set<Relationship>> outgoingUnreifiedRels = null;
    private boolean relsUnreified = false;
	 
    /**
     * Creates a new Data Bean, initializing all of the attributes.
     */
	public ProtegeDataBean(OWLModelManager mm, OWLEditorKit ek, OWLOntology o) {
		super();
		this.modelManager = mm;
		this.editorKit = ek;
		this.ontology = o;
		initStorage();
	}

	public ProtegeDataBean(URI projectURI) {
		super();
		JambalayaView view = JambalayaView.globalInstance();
		if (view != null) {
			modelManager = view.getOWLModelManager();
			editorKit = view.getOWLEditorKit();
			OWLOntologyManager om = modelManager.getOWLOntologyManager();
			if (om != null) {
				try {
					ontology = om.loadOntologyFromOntologyDocument(IRI.create(projectURI));
					modelManager.setActiveOntology(ontology);
					modelManager.fireEvent(EventType.ONTOLOGY_LOADED);
				}
				catch (OWLOntologyCreationException ex) {
				}
			}
		}
		initStorage();
	}

	private void initStorage()
	{
		rootEntities = new HashSet();
		metrics = new ProtegeMetrics(this.ontology);
		clsWidgets = new HashMap();
		frameToBrowserText = new HashMap();
		frameToArtifactTypeMap = new HashMap();
		relTypeToGroupMap = new HashMap();
		initRelTypeToGroupMappings();
		domainRangeRelsBuffer = new HashSet<Relationship>();
		incomingUnreifiedRels = new HashMap<Artifact, Set<Relationship>>();
		outgoingUnreifiedRels = new HashMap<Artifact, Set<Relationship>>();
		relsUnreified = false;
	}
	
    /**
     * @see ca.uvic.csr.shrimp.DataBean.DataBean#clearBufferedData()
     */
    public void clearBufferedData() {
        if (dataIsDirty()) {
            super.clearBufferedData();
            metrics.clear();
            clsWidgets.clear();
            frameToBrowserText.clear();
            frameToArtifactTypeMap.clear();
            relTypeToGroupMap.clear();
				initRelTypeToGroupMappings();
				domainRangeRelsBuffer.clear();
				incomingUnreifiedRels.clear();
				outgoingUnreifiedRels.clear();
            relsUnreified = false;
        }
    }

    public OWLModelManager getModelManager() {
        return modelManager;
    }

    public OWLOntology getProject() {
        return ontology;
    }

    public ProtegeMetrics getMetrics() {
        return metrics;
    }

    public Set getRootEntities() {
        return rootEntities;
    }

    /**
     * @param ent the root classes or instances
     */
    public void setRootClses(Set<OWLEntity> ent) {
        if (! rootEntities.equals(ent)) {
            setDataIsDirty();
            clearBufferedData();
            rootEntities = new HashSet(ent);

            validSlotSuperClsesAndTypes = new HashSet();
            for (OWLEntity e : rootEntities) {
					if (e instanceof OWLClass) {
						for (OWLClassExpression exp: ((OWLClass) e).getSuperClasses(ontology)) {
							if (exp.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS))
								validSlotSuperClsesAndTypes.add((OWLClass) exp);
						}
					} else if (e instanceof OWLNamedIndividual) {
						for (OWLClassExpression exp: ((OWLNamedIndividual) e).getTypes(ontology)) {
							if (exp.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS))
								validSlotSuperClsesAndTypes.add((OWLClass) exp);
						}
					}
				}

            fireRootArtifactsChangeEvent(rootEntities);
        }
    }

    /**
     * default root classes are any subclasses of :THING that are not system
     * classes
     *
     * @return A set of clses
     */
    public Set<OWLEntity> getDefaultRootEntities() {
        Set<OWLEntity> defaultRootEntities = new HashSet<OWLEntity>();
        defaultRootEntities.addAll(ontology.getClassesInSignature());
        defaultRootEntities.addAll(ontology.getIndividualsInSignature());
        return defaultRootEntities;
    }

    public void setDefaultRootClses() {
        setRootClses(getDefaultRootEntities());
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#findRootArtifactsInBackEnd(java.lang.String)
     */
    protected Vector findRootArtifactsInBackEnd(String[] cprels) {
        Vector rootArts = new Vector();

        // if is the "is-a" or just the "subclass of" hierarhcy then use the
        // root clses as the roots
        if (CollectionUtils.haveSameElements(cprels, SUBCLASS_OF_AND_INSTANCE_OF_HIERARCHY) || 
            CollectionUtils.haveSameElements(cprels, SUBCLASS_OF_HIERARCHY)) {
            for (OWLEntity e : rootEntities) {
                Artifact art = findArtifact(e, true, true, true);
                if (art != null) {
                    rootArts.addElement(art);
                }
            }
        } else if (cprels.length == 0) {
            rootArts.addAll(getArtifacts(true));
        } else {
            rootArts = defaultFindRootArtifactsInBackEnd(cprels);
        }
        return rootArts;
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#getArtifactsFromBackEnd()
     */
    protected Vector findAllArtifactsInBackEnd() {
        Set backEndArts = new HashSet();
		  OWLObjectHierarchyProvider mng = modelManager.getOWLHierarchyManager().getOWLClassHierarchyProvider();
        // for each root cls, find all its subclasses and all its instances
        for (OWLEntity e : rootEntities) {
            Artifact art = findArtifact(e, true, false, true);
            if (art != null) {
                backEndArts.add(art);
                if (e instanceof OWLClass) {
                    for (Object obj : mng.getDescendants((OWLClass) e)) {
                        if (obj instanceof OWLClass) {
									Artifact a = findArtifact((OWLClass) obj, true, false, true);
									if (a != null)
										 backEndArts.add(a);
                        }
                    }
                    for (OWLIndividual ind : ((OWLClass) e).getIndividuals(ontology)) {
                        if (ind.isNamed()) {
                            Artifact a = findArtifact((OWLNamedIndividual) ind, true, false, true);
                            if (a != null)
                                backEndArts.add(a);
                        }
                    }
                }
            }
				if (ProgressDialog.isCancelled())
					break;
            ProgressDialog.setNote("found " + backEndArts.size() + 
															(backEndArts.size() == 1 ? " item" : " items") + " so far ...");
        }
        return new Vector(backEndArts);
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#findArtifactTypesInBackEnd()
     */
    protected Vector findArtifactTypesInBackEnd() {
        Vector types = new Vector();
        types.add(CLASS_ART_TYPE);
        types.add(INSTANCE_ART_TYPE);
        return types;
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#findRelationshipTypesInBackEnd()
     */
    protected Vector findRelationshipTypesInBackEnd() {
        return new Vector(relTypeToGroupMap.keySet());
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#getDefaultGroupForRelationshipType(java.lang.String)
     */
    public String getDefaultGroupForRelationshipType(String relType) {
        String groupName = (String) relTypeToGroupMap.get(relType);
        if (groupName == null)
            System.err.println("Warning! No group for relationship type: \"" + relType + "\"");
        return groupName;
    }

    /*
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#findIncomingRelationshipsInBackEnd(ca.uvic.csr.shrimp.DataBean.Artifact)
     */
	protected Vector findIncomingRelationshipsInBackEnd(Artifact art) {
		Set<Relationship> relations = new HashSet<Relationship>();
		OWLEntity e = (art instanceof ProtegeArtifact ? ((ProtegeArtifact) art).getEntity() : null);
		if (e != null) {
			relations.addAll(loadSources(e, art));
			relations.addAll(loadDomainRangeRels(e, art, false));
			relations.addAll(findIncomingIndividualRelationships(e, art));
			relations.addAll(loadUnreifiedRelations(e, art, false));
			relations.addAll(findIncomingConditionsRelationships(e, art));
		}
		return new Vector(relations);
	}

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#findOutgoingRelationshipsInBackEnd(ca.uvic.csr.shrimp.DataBean.Artifact)
     */
	protected Vector findOutgoingRelationshipsInBackEnd(Artifact art) {
		Set<Relationship> relations = new HashSet<Relationship>();
		OWLEntity e = (art instanceof ProtegeArtifact ? ((ProtegeArtifact) art).getEntity() : null);
		if (e != null) {
			relations.addAll(loadBranches(e, art));
			relations.addAll(loadDomainRangeRels(e, art, true));
			relations.addAll(findOutgoingIndividualRelationships(e, art));
			relations.addAll(loadUnreifiedRelations(e, art, true));
			relations.addAll(findOutgoingConditionsRelationships(e, art));
		}
		return new Vector(relations);
	}
	
	private Set<Relationship> loadSources(OWLEntity e, Artifact art) {
		Set<Relationship> relations = new HashSet<Relationship>();
		OWLObjectHierarchyProvider mng = modelManager.getOWLHierarchyManager().getOWLClassHierarchyProvider();
		if (e instanceof OWLClass) {
			for (Object obj : mng.getParents((OWLClass) e)) {
				if (obj instanceof OWLClass) {
					Artifact fnd = findArtifact((OWLClass) obj);
					if (fnd != null)
						relations.add(createRel(null, fnd, art, DIRECT_SUBCLASS_SLOT_TYPE));
				}
			}
		}
		return relations;
	}
	
	private Set<Relationship> loadBranches(OWLEntity e, Artifact art) {
		Set<Relationship> relations = new HashSet<Relationship>();
		OWLObjectHierarchyProvider mng = modelManager.getOWLHierarchyManager().getOWLClassHierarchyProvider();
		if (e instanceof OWLClass) {
			for (Object obj : mng.getChildren((OWLClass) e)) {
				if (obj instanceof OWLClass) {
					Artifact fnd = findArtifact((OWLClass) obj);
					if (fnd != null)
						relations.add(createRel(null, art, fnd, DIRECT_SUBCLASS_SLOT_TYPE));
				}
			}
		}
		return relations;
	}
	
	/**
	 * Finds relationships between an artifact as a item in the domain, and the ranges of its
	 * properties
	 */
	private Set<Relationship> loadDomainRangeRels(OWLEntity e, Artifact art, boolean outgoing) {
		Set<Relationship> relations = new HashSet<Relationship>();
		if (domainRangeRelsBuffer.isEmpty())
			createDomainRangeRels();
		for (Relationship rel : domainRangeRelsBuffer) {
			Vector av = rel.getArtifacts();
			if (art.equals(av.get((outgoing ? 0 : 1))))
				relations.add(rel);
		}
		return relations;
	}
	
	private void createDomainRangeRels() {
		for (OWLObjectProperty property : ontology.getObjectPropertiesInSignature()) {
			for (OWLObjectProperty prp : property.getObjectPropertiesInSignature()) {
				String relType = modelManager.getRendering(prp) + SUFFIX_DOMAIN_RANGE;
				if (isRelationVisible(relType)) {
					relTypeToGroupMap.put(relType, GROUP_NAME_SLOT_TEMPLATE_ALLOWED_CLASS);
					
					Set<OWLClassExpression> domainVals = prp.getDomains(ontology);
					Set<OWLClassExpression> rangeVals = prp.getRanges(ontology);
					if (domainVals.isEmpty() && ! rangeVals.isEmpty())
						domainVals.add(modelManager.getOWLEntityFinder().getOWLClass("Thing"));
					else if (rangeVals.isEmpty() && ! domainVals.isEmpty())
						rangeVals.add(modelManager.getOWLEntityFinder().getOWLClass("Thing"));
						
					// make relationships between all named classes in the domain and all named classes in the range
					Set<OWLEntity> domains = getOWLClasses(domainVals);
					Set<OWLEntity> ranges = getOWLClasses(rangeVals);
					for (OWLEntity dc : domains) {
						Artifact src = findArtifact(dc);
						for (OWLEntity rc : ranges) {
							Artifact trg = findArtifact(rc);
							if (src != null && trg != null)
								domainRangeRelsBuffer.add(createRel(null, src, trg, relType));
						}
					}
				}
			}
		}
	}
	
	private Set<Relationship> findIncomingIndividualRelationships(OWLEntity e, Artifact art) {
		Set<Relationship> relations = new HashSet<Relationship>();
		if (e instanceof OWLNamedIndividual) {
			OWLNamedIndividual ind = (OWLNamedIndividual) e;
			for (OWLClassExpression exp : ind.getTypes(ontology)) {
				if (exp instanceof OWLClass) {
					Artifact fnd = findArtifact((OWLClass) exp);
					if (fnd != null)
						relations.add(createRel(null, fnd, art, DIRECT_INSTANCE_SLOT_TYPE));
				}
			}
		}
		return relations;
	}
	
	private Set<Relationship> findOutgoingIndividualRelationships(OWLEntity e, Artifact art) {
		Set<Relationship> relations = new HashSet<Relationship>();
		if (e instanceof OWLClass) {
			OWLClass cls = (OWLClass) e;
			for (OWLIndividual ind : cls.getIndividuals(ontology)) {
				if (ind.isNamed()) {
					Artifact fnd = findArtifact((OWLNamedIndividual) ind);
					if (fnd != null)
						relations.add(createRel(null, art, fnd, DIRECT_INSTANCE_SLOT_TYPE));
				}
			}
		}
		return relations;
	}
	
	private Set<Relationship> loadUnreifiedRelations(OWLEntity e, Artifact art, boolean outgoing) {
		if (! relsUnreified)
			unreifyRelationInstances();
		Map<Artifact, Set<Relationship>> map = (outgoing ? outgoingUnreifiedRels : incomingUnreifiedRels);
		Set<Relationship> relations = map.get(art);
		if (relations == null)
			relations = new HashSet<Relationship>();
		return relations;
	}
		
	private void unreifyRelationInstances() {
		for (OWLNamedIndividual ind : ontology.getIndividualsInSignature()) {
			Artifact src = findArtifact(ind);
			for (Entry<OWLObjectPropertyExpression, Set<OWLIndividual>> entry : 
																		ind.getObjectPropertyValues(ontology).entrySet()) {
				String relType = modelManager.getRendering(entry.getKey());
				if (isRelationVisible(relType)) {
					relTypeToGroupMap.put(relType + SUFFIX_SLOT_REIFIED_REL, GROUP_NAME_REIFIED_RELS);
					
					for (OWLIndividual val : entry.getValue()) {
						if (val.isNamed()) {
							Artifact trg = findArtifact((OWLNamedIndividual) val);
							if (src != null && trg != null) {
								Relationship rel = createRel(null, src, trg, relType);
								Set<Relationship> rels;
								
								rels = outgoingUnreifiedRels.get(src);
								if (rels == null) {
									rels = new HashSet<Relationship>();
									outgoingUnreifiedRels.put(src, rels);
								}
								rels.add(rel);
								
								rels = incomingUnreifiedRels.get(trg);
								if (rels == null) {
									rels = new HashSet<Relationship>();
									incomingUnreifiedRels.put(trg, rels);
								}
								rels.add(rel);
							}
						}
					}
				}
			}
		}
		relsUnreified = true;
	}
	
	private Set<Relationship> findIncomingConditionsRelationships(OWLEntity e, Artifact art) {
		Set<Relationship> relations = new HashSet<Relationship>();
		if (e instanceof OWLClass) {
			for (OWLAxiom axiom : ((OWLClass) e).getReferencingAxioms(ontology)) {
				if (axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
					OWLSubClassOfAxiom subClassAxiom = (OWLSubClassOfAxiom) axiom;
					OWLClassExpression subClassExpression = subClassAxiom.getSubClass();
					if (subClassExpression instanceof OWLClass) {
						OWLClassExpression superClassExpression = subClassAxiom.getSuperClass();
						if (superClassExpression instanceof OWLQuantifiedRestriction) {
							OWLQuantifiedRestriction restriction = (OWLQuantifiedRestriction) superClassExpression;
							if (restriction.getFiller() instanceof OWLClass) {
								String relType = modelManager.getRendering(restriction.getProperty());
								if (restriction instanceof OWLObjectSomeValuesFrom) 
									relType += SUB_CLASS_SOME_VALUE_OF;
								else 
									relType += SUB_CLASS_ALL_VALUES;
								if (isRelationVisible(relType)) {
									relTypeToGroupMap.put(relType, GROUP_NAME_SLOT_TEMPLATE_ALLOWED_CLASS);
									Artifact src = findArtifact((OWLClass) subClassExpression);
									Artifact trg = findArtifact((OWLClass) restriction.getFiller());
									if (src != null && trg != null)
										relations.add(createRel(null, src, trg, relType));
								}
							}
						}
					}
				}
			}
		}
		return relations;
	}
	
	private Set<Relationship> findOutgoingConditionsRelationships(OWLEntity e, Artifact art) {
		Set<Relationship> relations = new HashSet<Relationship>();
		if (e instanceof OWLClass) {
			OWLClass cls = (OWLClass) e;
			convertOWLClassExpressionsToArcs(cls, cls.getSuperClasses(ontology), relations, true);
			convertOWLClassExpressionsToArcs(cls, cls.getEquivalentClasses(ontology), relations, false);
		}
		return relations;
	}
	
	private void convertOWLClassExpressionsToArcs(OWLClass cls, Set<OWLClassExpression> expressions, 
																 Set<Relationship> relations, boolean origin) {
		for (OWLClassExpression exp : expressions) {
			if (exp.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM) 
					|| exp.getClassExpressionType().equals(ClassExpressionType.OBJECT_ALL_VALUES_FROM)) {
				convertOWLClassExpressionToArcs(cls, exp, relations, origin);
			}
			else if (exp.getClassExpressionType().equals(ClassExpressionType.OBJECT_INTERSECTION_OF)) {
				for(OWLClassExpression e : exp.asConjunctSet()) {
					convertOWLClassExpressionToArcs(cls, e, relations, origin);
				}
			}
		}
	}
	
	private void convertOWLClassExpressionToArcs(OWLClass cls, OWLClassExpression exp, 
																Set<Relationship> relations, boolean origin) {
		for (OWLClassExpression e : exp.asConjunctSet()) {
			if (e instanceof OWLQuantifiedRestriction) {
				OWLQuantifiedRestriction restriction = (OWLQuantifiedRestriction) e;
				if (restriction.getFiller() instanceof OWLClass) {
					String relType = modelManager.getRendering(restriction.getProperty());
					if (origin) {
						if (restriction instanceof OWLObjectSomeValuesFrom) 
							relType += SUB_CLASS_SOME_VALUE_OF;
						else 
							relType += SUB_CLASS_ALL_VALUES;
					}
					else {
						if (restriction instanceof OWLObjectSomeValuesFrom) 
							relType += EQUIVALENT_CLASS_SOME_VALUE_OF;
						else 
							relType += EQUIVALENT_CLASS_ALL_VALUES;
					}
					if (isRelationVisible(relType)) {
						relTypeToGroupMap.put(relType, GROUP_NAME_SLOT_TEMPLATE_ALLOWED_CLASS);
						Artifact src = findArtifact(cls);
						Artifact trg = findArtifact((OWLClass) restriction.getFiller());
						if (src != null && trg != null)
							relations.add(createRel(null, src, trg, relType));
					}
				}
			}
		}
	}
	
    /**
     * Returns the Protege Class Widget for the given frame. It is important to
     * keep all of the frames looking the same so the same widget is used in
     * each artifact representing the same frame.
     *
     * @param frame
     *            The frame to get the widget for
     */
    JComponent getClsWidget(OWLEntity e) {
		JComponent widget = (JComponent) clsWidgets.get(e);
		if (widget == null) {
			if (e instanceof OWLClass) {
				ArrayList exp = new ArrayList(); exp.add((OWLClass) e);
				OWLClassExpressionSetEditor ed = new OWLClassExpressionSetEditor(editorKit, exp);
				widget = ed.getEditorComponent();
			} else if (e instanceof OWLNamedIndividual) {
				OWLIndividualEditor ed = new OWLIndividualEditor(editorKit);
				ed.setEditedObject((OWLNamedIndividual) e);
				widget = ed.getEditorComponent();
			} else if (e instanceof OWLDataProperty) {
				OWLDataPropertyEditor ed = new OWLDataPropertyEditor(editorKit);
				ed.setEditedObject((OWLDataProperty) e);
				widget = ed.getEditorComponent();
			} else if (e instanceof OWLObjectProperty) {
				OWLObjectPropertyEditor ed = new OWLObjectPropertyEditor(editorKit);
				ed.setEditedObject((OWLObjectProperty) e);
				widget = ed.getEditorComponent();
			} else if (e instanceof OWLAnnotation) {
				OWLAnnotationEditor ed = new OWLAnnotationEditor(editorKit);
				ed.setEditedObject((OWLAnnotation) e);
				widget = ed.getEditorComponent();
			}
			if (widget != null)
				clsWidgets.put(e, widget);
		}

		return widget;
    }

    /**
     * Saves the ontology back to file
     */
	public void save() {
		if (modelManager != null && ontology != null) {
			try {
				modelManager.save(ontology);
			}
			catch (Exception ex) {
			}
		}
	}

    /**
     * @see ca.uvic.csr.shrimp.DataBean.DataBean#getArtifactExternalIDFromString(java.lang.String)
     */
    public Object getArtifactExternalIDFromString(String IDInStringForm) {
        return IDInStringForm;
    }

    /*
     * (non-Javadoc)
     *
     * @see ca.uvic.csr.shrimp.DataBean.DataBean#getStringFromExternalArtifactID(java.lang.Object)
     */
    public String getStringFromExternalArtifactID(Object externalID) {
        return externalID.toString();
    }

    /*
     * (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DataBean.DataBean#getStringFromExternalRelationshipID(java.lang.Object)
     */
    public String getStringFromExternalRelationshipID(Object externalID) {
        return ""; // protege relationships dont have and external id
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.DataBean#getDefaultCprels()
     */
    public String[] getDefaultCprels() {
        String[] cprels = { DIRECT_SUBCLASS_SLOT_TYPE, DIRECT_INSTANCE_SLOT_TYPE };
        return cprels;
    }

    public boolean getDefaultCprelsInverted() {
        return false;
    }

    /** Returns the browser text for a frame. */
    protected String getBrowserText(OWLEntity e) {
        if (e == null) {
            return "null";
        }
        String browserText = (String) frameToBrowserText.get(e);
        if (browserText == null) {
            browserText = modelManager.getRendering(e);
            frameToBrowserText.put(e, browserText);
        }
        return browserText;
    }

	 public OWLEntity findEntity(String id) {
		Set<OWLEntity> found = modelManager.getOWLEntityFinder().getEntities(IRI.create(id));
		if (! found.isEmpty())
			return found.iterator().next();
		return null;
	 }

    protected Artifact createArtifact(OWLEntity e) {
        ProtegeArtifact art = new ProtegeArtifact(this, e);
        return art;
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#createArtifact(Object)
     */
    protected Artifact createArtifact(Object externalId) {
        OWLEntity e = findEntity((String) externalId);
        if (e == null) {
            return null;
        }
        return createArtifact(e);
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#createEmptyRelationship(Object,
     *      java.lang.String, java.lang.String, java.util.Vector)
     */
    protected Relationship createEmptyRelationship(Object externalId, String name, String type, Vector artifacts) {
        ProtegeRelationship rel = new ProtegeRelationship(this, name, type, artifacts, externalId);
        return rel;
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#findArtifactByExternalId(java.lang.Object)
     */
    public Artifact findArtifactByExternalId(Object externalId) {
        if (!(externalId instanceof String)) {
            return null;
        }
        return findArtifact((String)externalId, true, true, true);
    }

    public Artifact findArtifact(OWLEntity e) {
        return findArtifact(e, true, true, true);
    }

    protected Artifact findArtifact(OWLEntity e, boolean checkShouldImportArtType, boolean checkIsInRootEntities, boolean checkIsHidden) {
        String externalId = frameToExternalArtifactID(e);
        return findArtifact(e, externalId, checkShouldImportArtType, checkIsInRootEntities, checkIsHidden);
    }

    protected Artifact findArtifact(String externalId, boolean checkShouldImportArtType, boolean checkIsInRootEntities, boolean checkIsHidden) {
        OWLEntity e = findEntity(externalId);
        return findArtifact(e, externalId, checkShouldImportArtType, checkIsInRootEntities, checkIsHidden);
    }

    protected Artifact findArtifact(OWLEntity e, String externalId, boolean checkShouldImportArtType, boolean checkIsInRootEntities, boolean checkIsHidden) {
        // check if we should be considering this type of frame
        if (e == null) {
            // do nothing
            return null;
        } else if (shouldImportFrameAsArtifact(e, checkShouldImportArtType, checkIsInRootEntities, checkIsHidden)) {
            Artifact art = null;
            Long internalIdLong = (Long)bufferExternalToInternalIdMap.get(externalId);
            if (internalIdLong != null) {
                art = getArtifactReference (internalIdLong);
            }
            if (art == null) {
                art = createArtifact (externalId);
                if (art != null) {
                    addArtifactReference (art);
                }
            }
            return art;
        }
        return null;
    }

    public boolean shouldImportFrameAsArtifact(OWLEntity e) {
        return shouldImportFrameAsArtifact(e, true, true, true);
    }

    /**
     * Determines if a frame should be considered for use in shrimp
     */
    public boolean shouldImportFrameAsArtifact(OWLEntity e, boolean checkShouldImportArtType) {
        return shouldImportFrameAsArtifact(e, checkShouldImportArtType, true, true);
    }

    /**
     * Determines if a frame should be considered for use in shrimp
     */
    public boolean shouldImportFrameAsArtifact(OWLEntity e, boolean checkShouldImportArtType, boolean checkIsInRootEntities) {
        return shouldImportFrameAsArtifact(e, checkShouldImportArtType, checkIsInRootEntities, true);
    }

    /**
     * Determines if a frame should be considered for use in shrimp
     */
    public boolean shouldImportFrameAsArtifact(OWLEntity e, boolean checkShouldImportArtType, boolean checkIsInRootEntities, boolean checkIsHidden) {
        boolean shouldImport = true;
        if (shouldImport && checkShouldImportArtType) {
            shouldImport = shouldImport && isProperArtifactType(e);
        }
        if (shouldImport && checkIsInRootEntities) {
            shouldImport = shouldImport && isInRootEntities(e);
        }
        if (shouldImport && checkIsHidden) {
            shouldImport = shouldImport && !isHidden(e);
        }

        return shouldImport;
    }

    protected boolean isHidden(OWLEntity e) {
        return false;
    }

    protected boolean isInRootEntities(OWLEntity e) {
        if (e instanceof OWLClass) {
            OWLClass cls = (OWLClass) e;
            for (OWLEntity itm : rootEntities) {
            	if (itm instanceof OWLClass) {
					    OWLClass rootCls = (OWLClass) itm;
	                if (cls == rootCls || cls.getSuperClasses(ontology).contains(rootCls))
	                    return true;
	                else if (cls.toStringID().equals(rootCls.toStringID()))
	                    return true;
               }
            }
        } else if (e instanceof OWLNamedIndividual) {
            OWLNamedIndividual ind = (OWLNamedIndividual) e;
            for (OWLEntity itm : rootEntities) {
                if (itm == ind)
	                 return true;
                else if (itm instanceof OWLClass && ind.getTypes(ontology).contains((OWLClass) itm))
	                 return true;
            }
        }
        return false;
    }

    private boolean isProperArtifactType(OWLEntity e) {
        return !isArtTypeFiltered(frameToArtifactType(e));
    }

    /**
     * Returns the type of the artifact that will represent the given frame.
     */
    protected String frameToArtifactType(OWLEntity e) {
        String type = (String) frameToArtifactTypeMap.get(e);
        if (type == null) {
            if (e instanceof OWLClass)
                type = CLASS_ART_TYPE;
            else if (e instanceof OWLNamedIndividual)
                type = INSTANCE_ART_TYPE;
            else
                type = UNKNOWN_ART_TYPE;
            frameToArtifactTypeMap.put(e, type);
        }
        return type;
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.DataBean#getArtifactIcon(ca.uvic.csr.shrimp.DataBean.Artifact)
     */
    public Icon getArtifactIcon(Artifact art) {
        Icon icon = null;
        OWLEntity e = (art instanceof ProtegeArtifact ? ((ProtegeArtifact) art).getEntity() : null);
        if (e != null) {
				OWLIconProviderImpl iconProvider = new OWLIconProviderImpl(modelManager);
				icon = iconProvider.getIcon(e);
        }
        return icon;
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#addRelationshipToBackEnd(ca.uvic.csr.shrimp.DataBean.Relationship)
     */
    protected boolean addRelationshipToBackEnd(Relationship relationship) {
        // TODO implement ProtegeDataBean.addRelationshipToBackEnd
        return false;
    }

    /**
     * Removes the relationship from protege
     */
    protected boolean removeRelationshipFromBackEnd(Relationship relationship) {
        // TODO implement ProtegeDataBean.removeRelationshipFromBackEnd
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#getParents(ca.uvic.csr.shrimp.DataBean.Artifact,
     *      java.lang.String[])
     */
    public Vector getParents(Artifact artifact, String[] cprels) {
        if (isFasterWayToCount(artifact, cprels)) {
            List cprelsList = Arrays.asList(cprels);
            OWLEntity e = ((ProtegeArtifact) artifact).getEntity();
            Vector potentialParentFrames = new Vector();
            OWLObjectHierarchyProvider mng = modelManager.getOWLHierarchyManager().getOWLClassHierarchyProvider();
            if (e instanceof OWLClass) {
                if (cprelsList.contains(DIRECT_SUBCLASS_SLOT_TYPE)) {
                    for (Object obj : mng.getParents((OWLClass) e)) {
                        if (obj instanceof OWLClass) {
									if (isValidSubClass((OWLClass) e, (OWLClass) obj))
										 potentialParentFrames.add((OWLClass) obj);
                        }
                    }
                }
            } else if (e instanceof OWLNamedIndividual) {
                if (cprelsList.contains(DIRECT_INSTANCE_SLOT_TYPE)) {
                    OWLNamedIndividual ind = (OWLNamedIndividual) e;
                    for (OWLClassExpression exp : ind.getTypes(ontology)) {
                        if (exp instanceof OWLClass)
                            potentialParentFrames.add((OWLClass) exp);
                    }
                }
            }
            Vector parents = new Vector();
            for (Iterator iter = potentialParentFrames.iterator(); iter.hasNext();) {
                OWLEntity potentialParent = (OWLEntity) iter.next();
                Artifact parentArt = findArtifact(potentialParent, true, true, true);
                // shouldn't be duplicates, takes too long to do the .contains
                if (parentArt != null /* && !parents.contains(parentArt) */) {
                    parents.add(parentArt);
                }
            }
            //System.out.println("parents of '" + getBrowserText(frame) + "' =
            // " + parents);
            return parents;
        }
        return super.getParents(artifact, cprels);
    }

    /*
     * (non-Javadoc)
     *
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#getChildren(ca.uvic.csr.shrimp.DataBean.Artifact,
     *      java.lang.String[])
     */
    public Vector getChildren(Artifact artifact, String[] cprels) {
        if (isFasterWayToCount(artifact, cprels)) {
            List cprelsList = Arrays.asList(cprels);
            OWLEntity e = ((ProtegeArtifact) artifact).getEntity();
            Vector potentialChildFrames = new Vector();
            OWLObjectHierarchyProvider mng = modelManager.getOWLHierarchyManager().getOWLClassHierarchyProvider();
            if (e instanceof OWLClass) {
                if (cprelsList.contains(DIRECT_SUBCLASS_SLOT_TYPE)) {
                    for (Object obj : mng.getChildren((OWLClass) e)) {
                        if (obj instanceof OWLClass) {
									if (isValidSubClass((OWLClass) e, (OWLClass) obj))
										 potentialChildFrames.add((OWLClass) obj);
                        }
                    }
                }
                if (cprelsList.contains(DIRECT_INSTANCE_SLOT_TYPE)) {
                    for (OWLIndividual ind : ((OWLClass) e).getIndividuals(ontology)) {
                        if (ind.isNamed())
                            potentialChildFrames.add((OWLNamedIndividual) ind);
                    }
                }
            }
            Vector children = new Vector();
            //int i = 0;
            for (Iterator iter = potentialChildFrames.iterator(); iter.hasNext();) {
                // System.out.println(i++);
                OWLEntity potentialChild = (OWLEntity) iter.next();
                Artifact childArt = findArtifact(potentialChild, true, false, true);
                if (childArt != null /* && !children.contains(childArt) */) { //shouldn't be duplicates
                    children.add(childArt);
                }
            }
            return children;
        }
        return super.getChildren(artifact, cprels);
    }

    protected boolean isValidSubClass(OWLClass cls, OWLClass subCls) {
        return !cls.equals(subCls);
    }

    protected boolean isFasterWayToCount(Artifact artifact, String[] cprels) {
        List cprelsList = Arrays.asList(cprels);
        boolean countInstances = cprelsList.contains(DIRECT_INSTANCE_SLOT_TYPE);
        boolean countSubClasses = cprelsList.contains(DIRECT_SUBCLASS_SLOT_TYPE);
        OWLEntity e = ((ProtegeArtifact) artifact).getEntity();
        boolean rightType = (e instanceof OWLNamedIndividual) || (e instanceof OWLClass);
        boolean fasterWayToCount = rightType && (cprelsList.size() == 2 && countInstances && countSubClasses) || (cprelsList.size() == 1 && (countInstances || countSubClasses));
        return fasterWayToCount;
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#getChildrenCount(Artifact,
     *      String []) Overriden to make a bit faster for large knowledge bases.
     *      We can get count of children quicker if we don't pull in all
     *      relationships.
     */
    public int getChildrenCount(Artifact artifact, String[] cprels) {
        if (cprels.length == 0) {
			return 0;
		}
        int childrenCount = 0;
        OWLEntity e = ((ProtegeArtifact) artifact).getEntity();
        if (isFasterWayToCount(artifact, cprels)) {
            List cprelsList = Arrays.asList(cprels);
            boolean countInstances = cprelsList.contains(DIRECT_INSTANCE_SLOT_TYPE);
            boolean countSubClasses = cprelsList.contains(DIRECT_SUBCLASS_SLOT_TYPE);
            childrenCount = getFasterChildrenCount(e, countInstances, countSubClasses);
            // add cprels to attribute name because number of children depends on the given cprels
            String attrName = AttributeConstants.ORD_ATTR_NUM_CHILDREN + ShrimpUtils.cprelsToKey(cprels);
            artifact.setAttribute(attrName, new Integer(childrenCount));
        } else {
            childrenCount = super.getChildrenCount(artifact, cprels);
        }
        return childrenCount;
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#getDescendentsCount(ca.uvic.csr.shrimp.DataBean.Artifact,
     *      java.lang.String[]) Overriden to make a bit faster for large
     *      knowledge bases. We can get count of descendents quicker if we don't
     *      pull in all relationships.
     */
    /**
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#getDescendentsCount(ca.uvic.csr.shrimp.DataBean.Artifact,
     *      java.lang.String[], boolean)
     */
    public int getDescendentsCount(Artifact artifact, String[] cprels, boolean countArtifactMultipleTimes) {
        if (cprels.length == 0) {
            return 0;
        }
        OWLEntity e = ((ProtegeArtifact) artifact).getEntity();
        int numDes = 0;
        if (isFasterWayToCount(artifact, cprels)) {
            List cprelsList = Arrays.asList(cprels);
            boolean countInstances = cprelsList.contains(DIRECT_INSTANCE_SLOT_TYPE);
            numDes = getFasterDescendentsCountRecursive(e, countInstances, countArtifactMultipleTimes);
            // add cprels to attribute name because number of descendents depends on the given cprels
            String attrName = AttributeConstants.ORD_ATTR_NUM_DESCENDENTS + ShrimpUtils.cprelsToKey(cprels);
            artifact.setAttribute(attrName, new Integer(numDes));
        } else {
            numDes = getDescendentsCountRecursive(artifact, cprels, new HashSet(), new HashSet(), countArtifactMultipleTimes);
        }
        return numDes;
    }

    public int getFasterChildrenCount(OWLEntity e, boolean countInstances, boolean countSubClasses) {
        int childrenCount = 0;
         OWLObjectHierarchyProvider mng = modelManager.getOWLHierarchyManager().getOWLClassHierarchyProvider();
         if (e instanceof OWLClass) {
            if (countSubClasses) {
                for (Object obj : mng.getChildren((OWLClass) e)) {
                    if (obj instanceof OWLClass) {
							  if (shouldImportFrameAsArtifact((OWLClass) obj, true))
									childrenCount++;
                    }
                }
            }
            if (countInstances /* && (CONSIDER_METACLASS_INSTANCES || !cls.isMetaCls()) */) {
                for (OWLIndividual ind : ((OWLClass) e).getIndividuals(ontology)) {
                    if (ind.isNamed () && shouldImportFrameAsArtifact((OWLNamedIndividual) ind, true)) {
                        childrenCount++;
                    }
                }
            }
        }
        return childrenCount;
    }

    protected void getFasterDescendentsInstancesCountRecursive (OWLEntity e, boolean countInstances, Set seenSoFar, boolean countArtifactMultipleTimes, int [] instanceCount, int [] subclassCount) {
        if (e instanceof OWLClass) {
            seenSoFar.add(e);
            if (countInstances) {
                for (OWLIndividual ind : ((OWLClass) e).getIndividuals(ontology)) {
	                if (ind.isNamed () && shouldImportFrameAsArtifact((OWLNamedIndividual) ind, true, false, true)) {
	                    if ((countArtifactMultipleTimes || !seenSoFar.contains(ind))) {
                           seenSoFar.add(ind);
	                        instanceCount[0]++;
	                    }
	                }
	            }
            }
            OWLObjectHierarchyProvider mng = modelManager.getOWLHierarchyManager().getOWLClassHierarchyProvider();
            for (Object obj : mng.getDescendants((OWLClass) e)) {
                if (obj instanceof OWLClass) {
                    if (shouldImportFrameAsArtifact((OWLClass) obj, true, false, true)) {
                        if ((countArtifactMultipleTimes || !seenSoFar.contains((OWLClass) obj))) {
                            seenSoFar.add((OWLClass) obj);
                            subclassCount[0]++;
                        }
                    }
                }
            }
        }
    }


    protected int getFasterDescendentsCountRecursive(OWLEntity e, boolean countInstances, boolean countArtifactMultipleTimes) {
        int count = 0;
        int [] instancesCount = new int [] {0}; // using arrays so can pass by reference
        int [] subclassesCount = new int [] {0};
        getFasterDescendentsInstancesCountRecursive(e, countInstances, new HashSet(), countArtifactMultipleTimes, instancesCount, subclassesCount);
        if (countInstances) {
            count += instancesCount[0];
        }
        count += subclassesCount[0];
        return count;
    }

    /*
     *  (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DataBean.DataBean#getOrdinalAttrValues(java.lang.String[], boolean, boolean, int)
     */
    public Map getOrdinalAttrValues(String[] cprels, boolean lookInBackEnd, boolean inverted, int targetType) {
        Map values = super.getOrdinalAttrValues(cprels, lookInBackEnd, inverted, targetType);

        if (targetType != ARTIFACT_TYPE) {
            return values;
        }

        // we need to add a few more attributes for protege from the metrics
        Vector artifacts = getArtifacts(lookInBackEnd);

        //warn the user before doing this if many artifacts?

        SortedSet instancesCounts = new TreeSet();
        SortedSet inheritorCounts = new TreeSet();
        SortedSet depthCounts = new TreeSet();
        SortedSet strahlerCounts = new TreeSet();

        for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
            ProtegeArtifact art = (ProtegeArtifact) iter.next();

            //get from PM
            Integer instances = new Integer(metrics.getNumInstances(art.getEntity()));
            //now set it
            if (instances != null) {
                art.setAttribute(ProtegeArtifact.INSTANCES_KEY, instances);
                instancesCounts.add(instances);
            }

            Integer inheritors = new Integer(metrics.getNumInheritors(art.getEntity()));
            if (inheritors != null) {
                art.setAttribute(ProtegeArtifact.INHERITORS_KEY, inheritors);
                inheritorCounts.add(inheritors);
            }

            Integer strahler = new Integer(metrics.getStrahler(art.getEntity()));
            if (strahler != null) {
                art.setAttribute(ProtegeArtifact.STRAHLER_KEY, strahler);
                strahlerCounts.add(strahler);
            }

            Integer depth = new Integer(metrics.getTreeDepth(art.getEntity()));
            if (depth != null) {
                art.setAttribute(ProtegeArtifact.DEPTH_KEY, depth);
                depthCounts.add(depth);
            }
        }
        values.put(ProtegeArtifact.INSTANCES_KEY, instancesCounts);
        values.put(ProtegeArtifact.INHERITORS_KEY, inheritorCounts);
        values.put(ProtegeArtifact.DEPTH_KEY, depthCounts);
        values.put(ProtegeArtifact.STRAHLER_KEY, strahlerCounts);
        return values;
    }

    /* **** NOTE: the id of a protege node is the same as the name of the
     * frame that it represents, NOT the frame's id.
     * The reason for this is that for projects stored in a database the
     * frame id can change from session to session, whereas the frame name
     * stays the same. One issue that arises here, is that the frame name
     * can be changed by the user while editing a project.
     */
    public static String frameToExternalArtifactID(OWLEntity e) {
        return e.toStringID();
    }

    /**
     *
     * @param frame
     * @return The name to be given to the artifact associated with the given frame.
     */
    public String frameToArtifactName(OWLEntity e) {
        return getBrowserText(e);
    }

	private void initRelTypeToGroupMappings() {
		relTypeToGroupMap.put(DIRECT_INSTANCE_SLOT_TYPE, GROUP_NAME_CLASS_INSTANCE_HIERARCHY);
		relTypeToGroupMap.put(DIRECT_SUBCLASS_SLOT_TYPE, GROUP_NAME_CLASS_INSTANCE_HIERARCHY);
	}

	private boolean isRelationVisible(String rel) {
		return true;
	}
	
	private Set<OWLEntity> getOWLClasses(Set<OWLClassExpression> owlExpressions) {
		Set<OWLEntity> entities = new HashSet<OWLEntity>();
		for (OWLClassExpression exp : owlExpressions) {
			if (exp instanceof OWLClass)
				entities.add((OWLClass) exp);
		}
		return entities;
	}
	
    // for testing
    public static void main(String[] args) {
        ApplicationAccessor.setApplication(new AbstractShrimpApplication("", ""){});
        //String fileName = "C:\\Program Files\\Protege_2.1_beta_build_199\\examples\\testCount.pprj";
        //String fileName = "C:\\Program Files\\Protege_2.1_beta_build_199\\examples\\Thesaurus.pprj";
        String fileName = "demo\\newspaper\\newspaper.pprj";
        final ProtegeDataBean dataBean = new ProtegeDataBean(URI.create(fileName));
        dataBean.setDefaultRootClses();

        Vector leaves = dataBean.getLeafArtifacts(new String[] { "responsible_for" });
        for (Iterator iter = leaves.iterator(); iter.hasNext();) {
            Artifact leaf = (Artifact) iter.next();
            System.out.println(leaf);
        }
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#addArtifactToBackEnd(ca.uvic.csr.shrimp.DataBean.Artifact)
     */
    protected boolean addArtifactToBackEnd(Artifact artifact) {
        // TODO implement ProtegeDataBean.addArtifactToBackEnd
        return false;
    }

}
