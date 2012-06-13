/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.hl7.fhir.definitions.ecore.fhir.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.hl7.fhir.definitions.ecore.fhir.BindingDefn;
import org.hl7.fhir.definitions.ecore.fhir.BindingStrength;
import org.hl7.fhir.definitions.ecore.fhir.BindingType;
import org.hl7.fhir.definitions.ecore.fhir.DefinedCode;
import org.hl7.fhir.definitions.ecore.fhir.FhirPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Binding Defn</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.hl7.fhir.definitions.ecore.fhir.impl.BindingDefnImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.hl7.fhir.definitions.ecore.fhir.impl.BindingDefnImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.hl7.fhir.definitions.ecore.fhir.impl.BindingDefnImpl#getBinding <em>Binding</em>}</li>
 *   <li>{@link org.hl7.fhir.definitions.ecore.fhir.impl.BindingDefnImpl#getStrength <em>Strength</em>}</li>
 *   <li>{@link org.hl7.fhir.definitions.ecore.fhir.impl.BindingDefnImpl#getArtifactName <em>Artifact Name</em>}</li>
 *   <li>{@link org.hl7.fhir.definitions.ecore.fhir.impl.BindingDefnImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.hl7.fhir.definitions.ecore.fhir.impl.BindingDefnImpl#getSource <em>Source</em>}</li>
 *   <li>{@link org.hl7.fhir.definitions.ecore.fhir.impl.BindingDefnImpl#getCodes <em>Codes</em>}</li>
 *   <li>{@link org.hl7.fhir.definitions.ecore.fhir.impl.BindingDefnImpl#getDefinition <em>Definition</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class BindingDefnImpl extends EObjectImpl implements BindingDefn {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final int ID_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected int id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getBinding() <em>Binding</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBinding()
	 * @generated
	 * @ordered
	 */
	protected static final BindingType BINDING_EDEFAULT = BindingType.UNBOUND;

	/**
	 * The cached value of the '{@link #getBinding() <em>Binding</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBinding()
	 * @generated
	 * @ordered
	 */
	protected BindingType binding = BINDING_EDEFAULT;

	/**
	 * The default value of the '{@link #getStrength() <em>Strength</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStrength()
	 * @generated
	 * @ordered
	 */
	protected static final BindingStrength STRENGTH_EDEFAULT = BindingStrength.UNSTATED;

	/**
	 * The cached value of the '{@link #getStrength() <em>Strength</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStrength()
	 * @generated
	 * @ordered
	 */
	protected BindingStrength strength = STRENGTH_EDEFAULT;

	/**
	 * The default value of the '{@link #getArtifactName() <em>Artifact Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArtifactName()
	 * @generated
	 * @ordered
	 */
	protected static final String ARTIFACT_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getArtifactName() <em>Artifact Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArtifactName()
	 * @generated
	 * @ordered
	 */
	protected String artifactName = ARTIFACT_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getSource() <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
	protected static final String SOURCE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSource() <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
	protected String source = SOURCE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getCodes() <em>Codes</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCodes()
	 * @generated
	 * @ordered
	 */
	protected EList<DefinedCode> codes;

	/**
	 * The default value of the '{@link #getDefinition() <em>Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefinition()
	 * @generated
	 * @ordered
	 */
	protected static final String DEFINITION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDefinition() <em>Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefinition()
	 * @generated
	 * @ordered
	 */
	protected String definition = DEFINITION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BindingDefnImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return FhirPackage.Literals.BINDING_DEFN;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(int newId) {
		int oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FhirPackage.BINDING_DEFN__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FhirPackage.BINDING_DEFN__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BindingType getBinding() {
		return binding;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBinding(BindingType newBinding) {
		BindingType oldBinding = binding;
		binding = newBinding == null ? BINDING_EDEFAULT : newBinding;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FhirPackage.BINDING_DEFN__BINDING, oldBinding, binding));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BindingStrength getStrength() {
		return strength;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStrength(BindingStrength newStrength) {
		BindingStrength oldStrength = strength;
		strength = newStrength == null ? STRENGTH_EDEFAULT : newStrength;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FhirPackage.BINDING_DEFN__STRENGTH, oldStrength, strength));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getArtifactName() {
		return artifactName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setArtifactName(String newArtifactName) {
		String oldArtifactName = artifactName;
		artifactName = newArtifactName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FhirPackage.BINDING_DEFN__ARTIFACT_NAME, oldArtifactName, artifactName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FhirPackage.BINDING_DEFN__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getSource() {
		return source;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSource(String newSource) {
		String oldSource = source;
		source = newSource;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FhirPackage.BINDING_DEFN__SOURCE, oldSource, source));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<DefinedCode> getCodes() {
		if (codes == null) {
			codes = new EObjectContainmentEList<DefinedCode>(DefinedCode.class, this, FhirPackage.BINDING_DEFN__CODES);
		}
		return codes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDefinition(String newDefinition) {
		String oldDefinition = definition;
		definition = newDefinition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, FhirPackage.BINDING_DEFN__DEFINITION, oldDefinition, definition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case FhirPackage.BINDING_DEFN__CODES:
				return ((InternalEList<?>)getCodes()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case FhirPackage.BINDING_DEFN__ID:
				return getId();
			case FhirPackage.BINDING_DEFN__NAME:
				return getName();
			case FhirPackage.BINDING_DEFN__BINDING:
				return getBinding();
			case FhirPackage.BINDING_DEFN__STRENGTH:
				return getStrength();
			case FhirPackage.BINDING_DEFN__ARTIFACT_NAME:
				return getArtifactName();
			case FhirPackage.BINDING_DEFN__DESCRIPTION:
				return getDescription();
			case FhirPackage.BINDING_DEFN__SOURCE:
				return getSource();
			case FhirPackage.BINDING_DEFN__CODES:
				return getCodes();
			case FhirPackage.BINDING_DEFN__DEFINITION:
				return getDefinition();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case FhirPackage.BINDING_DEFN__ID:
				setId((Integer)newValue);
				return;
			case FhirPackage.BINDING_DEFN__NAME:
				setName((String)newValue);
				return;
			case FhirPackage.BINDING_DEFN__BINDING:
				setBinding((BindingType)newValue);
				return;
			case FhirPackage.BINDING_DEFN__STRENGTH:
				setStrength((BindingStrength)newValue);
				return;
			case FhirPackage.BINDING_DEFN__ARTIFACT_NAME:
				setArtifactName((String)newValue);
				return;
			case FhirPackage.BINDING_DEFN__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case FhirPackage.BINDING_DEFN__SOURCE:
				setSource((String)newValue);
				return;
			case FhirPackage.BINDING_DEFN__CODES:
				getCodes().clear();
				getCodes().addAll((Collection<? extends DefinedCode>)newValue);
				return;
			case FhirPackage.BINDING_DEFN__DEFINITION:
				setDefinition((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case FhirPackage.BINDING_DEFN__ID:
				setId(ID_EDEFAULT);
				return;
			case FhirPackage.BINDING_DEFN__NAME:
				setName(NAME_EDEFAULT);
				return;
			case FhirPackage.BINDING_DEFN__BINDING:
				setBinding(BINDING_EDEFAULT);
				return;
			case FhirPackage.BINDING_DEFN__STRENGTH:
				setStrength(STRENGTH_EDEFAULT);
				return;
			case FhirPackage.BINDING_DEFN__ARTIFACT_NAME:
				setArtifactName(ARTIFACT_NAME_EDEFAULT);
				return;
			case FhirPackage.BINDING_DEFN__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case FhirPackage.BINDING_DEFN__SOURCE:
				setSource(SOURCE_EDEFAULT);
				return;
			case FhirPackage.BINDING_DEFN__CODES:
				getCodes().clear();
				return;
			case FhirPackage.BINDING_DEFN__DEFINITION:
				setDefinition(DEFINITION_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case FhirPackage.BINDING_DEFN__ID:
				return id != ID_EDEFAULT;
			case FhirPackage.BINDING_DEFN__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case FhirPackage.BINDING_DEFN__BINDING:
				return binding != BINDING_EDEFAULT;
			case FhirPackage.BINDING_DEFN__STRENGTH:
				return strength != STRENGTH_EDEFAULT;
			case FhirPackage.BINDING_DEFN__ARTIFACT_NAME:
				return ARTIFACT_NAME_EDEFAULT == null ? artifactName != null : !ARTIFACT_NAME_EDEFAULT.equals(artifactName);
			case FhirPackage.BINDING_DEFN__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case FhirPackage.BINDING_DEFN__SOURCE:
				return SOURCE_EDEFAULT == null ? source != null : !SOURCE_EDEFAULT.equals(source);
			case FhirPackage.BINDING_DEFN__CODES:
				return codes != null && !codes.isEmpty();
			case FhirPackage.BINDING_DEFN__DEFINITION:
				return DEFINITION_EDEFAULT == null ? definition != null : !DEFINITION_EDEFAULT.equals(definition);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (id: ");
		result.append(id);
		result.append(", name: ");
		result.append(name);
		result.append(", binding: ");
		result.append(binding);
		result.append(", strength: ");
		result.append(strength);
		result.append(", artifactName: ");
		result.append(artifactName);
		result.append(", description: ");
		result.append(description);
		result.append(", source: ");
		result.append(source);
		result.append(", definition: ");
		result.append(definition);
		result.append(')');
		return result.toString();
	}

} //BindingDefnImpl
